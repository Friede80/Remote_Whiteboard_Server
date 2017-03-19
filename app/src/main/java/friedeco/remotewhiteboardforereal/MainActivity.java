package friedeco.remotewhiteboardforereal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity
{
    ServerSocket serverSocket;
    View         onlineIndicator;
    DrawingView  drawingView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        onlineIndicator = findViewById( R.id.onlineIndicatorView );
        drawingView = (DrawingView)findViewById( R.id.drawing );

        //Hide the navigation and status bars
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                                  | View.SYSTEM_UI_FLAG_FULLSCREEN );

        Thread socketServerThread = new Thread( new SocketServerThread() );
        socketServerThread.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread
    {

        static final int SocketServerPORT = 51111;
        int count = 0;

        @Override
        public void run()
        {
            try
            {
                serverSocket = new ServerSocket( SocketServerPORT );

                while( true )
                {
                    Socket socket = serverSocket.accept();
                    DataInputStream socketStream = new DataInputStream( socket.getInputStream() );
                    byte[] inputBuffer = new byte[10];

                    MainActivity.this.runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onlineIndicator.setBackgroundColor( Color.GREEN );
                        }
                    } );

                    while(true)
                    {
                        int bytesRead = socketStream.read( inputBuffer );
                        StringBuilder builder = new StringBuilder();
                        for(int i=0; i< bytesRead; i++) {
                            builder.append(String.format("%02x ", inputBuffer[i]));
                        }
                        Log.d("VALUES", builder.toString() );

                        if( bytesRead > 0 )
                        {
                            byte header = inputBuffer[0];
                            if( header == 0x01 || header == 0x02 )
                            {
                                float x = ByteBuffer.wrap(inputBuffer, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                float y = ByteBuffer.wrap(inputBuffer, 5, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                MainActivity.this.runOnUiThread( new updateView( header, x, y, 0) );
                            }
                            else if( header == 0x03 )
                            {
                                MainActivity.this.runOnUiThread( new updateView( header, 0, 0, 0) );
                            }
                            else if( header == 0x04 )
                            {
                                int color = ByteBuffer.wrap(inputBuffer, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                MainActivity.this.runOnUiThread( new updateView( header, 0, 0, color ) );
                            }
                            else if( header == 0x05 )
                            {
                                MainActivity.this.runOnUiThread( new updateView( header, 0, 0, 0 ) );
                            }
                        }
                        else
                        {
                            MainActivity.this.runOnUiThread( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onlineIndicator.setBackgroundColor( Color.RED );
                                }
                            } );
                            break;
                        }
                    }
                }
            }
            catch( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class updateView implements Runnable
    {
        byte mHeader;
        float mX;
        float mY;
        int mColor;

        updateView( byte aHeader, float aX, float aY, int aColor)
        {
            mHeader = aHeader;
            mX = aX;
            mY = aY;
            mColor = aColor;
        }
        public void run() {
            switch( mHeader )
            {
                case 0x01:
                    drawingView.pointerPressed( mX, mY );
                    break;
                case 0x02:
                    drawingView.pointerMoved( mX, mY );
                    break;
                case 0x03:
                    drawingView.pointerLifted();
                    break;
                case 0x04:
                    drawingView.setColor( mColor );
                    break;
                case 0x05:
                    drawingView.clear();
            }
        }
    }

}