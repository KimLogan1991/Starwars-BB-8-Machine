package org.oroca.teleopclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.mjpegsample.MjpegView.MjpegInputStream;
import de.mjpegsample.MjpegView.MjpegView;

public class teleopclient extends Activity implements OnClickListener{

	private EditText editTextIPAddress;
	private EditText editTextPort;
	private TextView textViewStatus;
	private Button buttonConnect;
	private Button buttonClose;
	private Button buttonUp;
	private Button buttonLeftTurn;
	private Button buttonRightTurn;
	private Button buttonDown;
	private Button buttonStop;
	Button button1;
	Button button2;
	Button button3;
	Button button4;
	Button button5;
	Button button6;
	Button button7;
	Button button8;
	Button button9;
	
	private InputMethodManager imm;
	private String server = "192.168.43.24";
	private int port = 500;
	private Socket socket;
	private OutputStream outs;
	private Thread rcvThread;
	public logger logger;
	private MjpegView mv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 mv = (MjpegView)findViewById(R.id.mjpegView1);
		String URL = "http://192.168.43.24:8080/?action=stream";



        mv.setSource(MjpegInputStream.read(URL));
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(true);
			
		editTextIPAddress = (EditText)this.findViewById(R.id.editTextIPAddress);
		editTextIPAddress.setText(server);
		editTextPort = (EditText)this.findViewById(R.id.editTextPort);
		//editTextPort.setText(port);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				
		textViewStatus = (TextView)this.findViewById(R.id.textViewStatus);
		textViewStatus.setText("TeleOp Client");
		
		logger = new logger(textViewStatus);
		
		buttonConnect   = (Button)this.findViewById(R.id.buttonConnect);
		buttonClose     = (Button)this.findViewById(R.id.buttonClose);
		//buttonUp        = (Button)this.findViewById(R.id.buttonUp);
		//buttonLeftTurn  = (Button)this.findViewById(R.id.buttonLeftTurn);
		//buttonRightTurn = (Button)this.findViewById(R.id.buttonRightTurn);
		//buttonDown      = (Button)this.findViewById(R.id.buttonDown);
		
		button1 = (Button)this.findViewById(R.id.Up);
		button2 = (Button)this.findViewById(R.id.Down);
		button3 = (Button)this.findViewById(R.id.Left);
		button4 = (Button)this.findViewById(R.id.Right);
		button5 = (Button)this.findViewById(R.id.Stop);
		
		
		buttonConnect.setOnClickListener(this);
		buttonClose.setOnClickListener(this);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		button5.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == buttonConnect)
		{
			imm.hideSoftInputFromWindow(editTextIPAddress.getWindowToken(), 0);
			
			try{
				if(socket!=null)
				{
					socket.close();
					socket = null;
				}
				
				server = editTextIPAddress.getText().toString();
				port = Integer.parseInt(editTextPort.getText().toString());
				socket = new Socket(server, port);
				outs = socket.getOutputStream();

				rcvThread = new Thread(new rcvthread(logger, socket));
    		    rcvThread.start();
				logger.log("Connected");
			} catch (IOException e){
				logger.log("Fail to connect");
				e.printStackTrace();
			}
		}
		
		if(arg0 == buttonClose)
		{
			imm.hideSoftInputFromWindow(editTextIPAddress.getWindowToken(), 0);
			
			if(socket!=null)
			{
				exitFromRunLoop();
				try{
					socket.close();
					socket = null;
					logger.log("Closed!");
					rcvThread = null;
				} catch (IOException e){
					logger.log("Fail to close");
					e.printStackTrace();
				}
			}
		}
		
		if(arg0 == button1 || arg0 == button2 || arg0 == button3 || arg0 == button4 || arg0 == button5 )
		{
			String sndOpkey = "CMD";
			
			if(arg0 == button1)	    sndOpkey = "Forward";
			if(arg0 == button2)	    sndOpkey = "Back";
			if(arg0 == button3)	    sndOpkey = "Left";
			if(arg0 == button4)	    sndOpkey = "Right";
			if(arg0 == button5)	    sndOpkey = "Stop";
			//if(arg0 == buttonLeftTurn)	sndOpkey = "LeftTurn";
			//if(arg0 == buttonRightTurn)	sndOpkey = "RightTurn";
			//if(arg0 == buttonDown)	    sndOpkey = "Down";

			try{
				outs.write(sndOpkey.getBytes("UTF-8"));
				outs.flush();
			} catch (IOException e){
				logger.log("Fail to send");
				e.printStackTrace();
			}
		}		
	}
	
    void exitFromRunLoop(){
    	try {
    		String sndOpkey = "[close]";
    		outs.write(sndOpkey.getBytes("UTF-8"));
    		outs.flush();
    	} catch (IOException e) {
			logger.log("Fail to send");
			e.printStackTrace();
    	}
    }
    
    public void onPause() {
		super.onPause();
		mv.stopPlayback();
	}
}
