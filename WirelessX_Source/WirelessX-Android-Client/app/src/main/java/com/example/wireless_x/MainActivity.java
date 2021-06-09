/*! \file 

    \brief This is where the main code of the Wireless-X android application is written.
*/
package com.example.wireless_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompatSideChannelService;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.security.auth.login.LoginException;

/*! \brief This is where the main code of the Wireless-X android application is written.
 *
 *  The MainActivity consists of the methods that initialize all the required variables and fields
 *  when the app starts, methods which keep listening to the mouse and keyboard events such as a mouse
 *  click event or a key press event, screen touch events, methods which send the camera frames to 
 *  the virtual camera device running on the laptop and so on.
*/
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private TextView textView;
    private PrintWriter output;
    private BufferedReader input;
    private Thread thread1 = null;

    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private static final int SERVER_PORT = 5000;
    private static String SERVER_IP = "";
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean camera_front= true;
    private LinearLayout keyboard_layout, camera_switch_radio_layout, ip_address_linear_layout, mouse_layout;
    private int screen_height;
    private int screen_width;
    private RadioButton cameraRadioButton;
    private RadioButton mouseRadioButton;
    private RadioButton cameraSwitchRadioButton;
    private EditText ip_address_editText;
    private Button test_Button;
    private TextView test_textView;
    private Button enter_wireless_x_button;
    private boolean doubleBackToExitPressedOnce = false;
    private String response="";
    private SharedPreferences preferences;
    private String MY_PREFS_NAME="ip_store";

    private Button wave_key, l_shift, r_shift, forward_slash_key, period_key, comma_key, semicolon_key, apostrophe_key, left_box_brac_key, capslock_key;
    private Button right_box_brac_key, back_slash_key, one_key, two_key, three_key, four_key, five_key, six_key, seven_key, eight_key, nine_key, zero_key, minus_key, equal_key;

    private float x, y;

    private boolean camera_enabled= false;
    private boolean mouse_enabled = false;
    private boolean shift_enabled = false;
    private boolean caps_enabled = false;

    private View mouse_space;

    private int mouse_space_height, mouse_space_width;

    private int desktop_width=1, desktop_height=1;


    /*! \brief OpenCV Initialization
    *
    *  Wireless-X works with OpenCV manager in an asynchronous manner, the onManagerConnected callback
    *  will be called in the UI thread once the initialization finishes.
    */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /*! \brief Sets up the app layout and contains the methods to handle various touch-related events.
    *
    *  Initializes all the app components and contains an listener for those events which can occur when
    *  the user interacts with the screen by single tap, double tap, scrolling or some gesture on the screen.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textview_first);
        keyboard_layout=findViewById(R.id.keyboard_layout);
        mouse_layout=findViewById(R.id.mouse_layout);

        camera_switch_radio_layout=findViewById(R.id.camera_switch_radio_layout);

        ip_address_editText=findViewById(R.id.ip_address_editText);
        test_Button=findViewById(R.id.test_Button);
        test_textView=findViewById(R.id.test_textView);
        enter_wireless_x_button=findViewById(R.id.enter_wireless_x_button);
        ip_address_linear_layout = findViewById(R.id.ip_address_linear_layout);

        wave_key=findViewById(R.id.wave_key);
        r_shift=findViewById(R.id.r_shift);
        l_shift=findViewById(R.id.l_shift);
        forward_slash_key=findViewById(R.id.forward_slash_key);
        period_key=findViewById(R.id.period_key);
        comma_key=findViewById(R.id.comma_key);
        semicolon_key=findViewById(R.id.semicolon_key);
        apostrophe_key=findViewById(R.id.apostrophe_key);
        left_box_brac_key=findViewById(R.id.left_box_brac_key);
        right_box_brac_key=findViewById(R.id.right_box_brac_key);
        back_slash_key=findViewById(R.id.back_slash_key);
        one_key=findViewById(R.id.one_key);
        two_key=findViewById(R.id.two_key);
        three_key=findViewById(R.id.three_key);
        four_key=findViewById(R.id.four_key);
        five_key=findViewById(R.id.five_key);
        six_key=findViewById(R.id.six_key);
        seven_key=findViewById(R.id.seven_key);
        eight_key=findViewById(R.id.eight_key);
        nine_key=findViewById(R.id.nine_key);
        zero_key=findViewById(R.id.zero_key);
        minus_key=findViewById(R.id.minus_key);
        equal_key=findViewById(R.id.equal_key);
        capslock_key=findViewById(R.id.capslock_key);
        mouse_space=findViewById(R.id.mouse_space);

        /*! \brief Method which listens for screen-touch related events.
        *
        *  When the user performs a double tap, it is translated to the double left-click on a physical mouse.
        *  Similarly, when the user performs a single tap, it's effect is same as a single click on any physical mouse.
        *  There is also an onScroll event which corresponds to the mouse scrolling event.
        *  This listener uses the GestureDetector class to handle such events.
        */
        mouse_space.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
                
                @Override
                /*! \brief Method to handle the double-tap event.
                *
                *  When the user performs a double tap, it is translated to the double left-click on a physical mouse.
                *  This is done by starting two threads simultaneously, which product the effect of two single-clicks
                *  without much delay, thus corresponding to a double-click.
                */
                public boolean onDoubleTap(MotionEvent e) {
                    Thread thread1 = new Thread(new SendMouseClicks("LEFT \nCLICK"));
                    thread1.start();
                    Thread thread2 = new Thread(new SendMouseClicks("LEFT \nCLICK"));
                    thread2.start();

                    return super.onDoubleTap(e);
                }

                @Override
                /*! \brief Method to handle the single-tap event.
                *
                *  When the user performs a single tap, it is translated to the single left-click on a physical mouse.
                *  This is done by starting a thread, which sends the event information to the server running on laptop
                *  and then the server acts accordingly.
                */
                public boolean onSingleTapConfirmed(MotionEvent e) {

                    //Log.e("TEST", "onSingleTapConfirmed");
                    Thread thread1 = new Thread(new SendMouseClicks("LEFT \nCLICK"));
                    thread1.start();

                    return super.onSingleTapConfirmed(e);
                }


                @Override
                /*! \brief Method to handle the mouse scrolling event.
                *
                *  When the user performs a scroll event, the coordinates are transferred to the server, which
                *  translates those coordinates to the position with respect to the laptop screen.
                */
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    x=e2.getX()/mouse_space_width;
                    y=e2.getY()/mouse_space_height;

                    if(x>=0 && x<=1 && y>=0 && y<=1)
                    {
                        Thread thread1 = new Thread(new SendMouseCoordinatesThread(x, y));
                        thread1.start();
                    }

                    return super.onScroll(e1, e2, distanceX, distanceY);
                }
            });

            @Override
            /*! \brief Method to handle the screen-touch event.
            *
            *  This method calls the GestureDetector object to handle the screen-touch event which can be
            *  any one of the single-tap, double-tap or scroll events.
            */
            public boolean onTouch(View view, MotionEvent motionEvent) {

                gestureDetector.onTouchEvent(motionEvent);

                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.i("TAG", "touched up");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        break;
                }

                return true;
            }
        });

        cameraRadioButton=findViewById(R.id.camera_radio);
        mouseRadioButton=findViewById(R.id.mouse_radio);
        cameraSwitchRadioButton=findViewById(R.id.camera_switch_radio);
    }
        
    /*! \brief Displays the keys which correspond to special characters.
     *
     *  When the shift-key is pressed on the keyboard in Wireless-X app, this method changes the layout
     *  of some keys to those keys which correspond to special characters such as brackets, '@', etc.
    */
    public void shiftPress(View view) {

        shift_enabled=!shift_enabled;

        if(shift_enabled)
        {
            l_shift.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            r_shift.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

            wave_key.setText("~");
            forward_slash_key.setText("?");
            period_key.setText(">");
            comma_key.setText("<");
            semicolon_key.setText(":");
            apostrophe_key.setText("\"");
            left_box_brac_key.setText("{");
            right_box_brac_key.setText("}");
            back_slash_key.setText("|");
            one_key.setText("!");
            two_key.setText("@");
            three_key.setText("#");
            four_key.setText("$");
            five_key.setText("%");
            six_key.setText("^");
            seven_key.setText("&");
            eight_key.setText("*");
            nine_key.setText("(");
            zero_key.setText(")");
            minus_key.setText("_");
            equal_key.setText("+");

            wave_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            forward_slash_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            period_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            comma_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            semicolon_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            apostrophe_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            left_box_brac_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            right_box_brac_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            back_slash_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            one_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            two_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            three_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            four_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            five_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            six_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            seven_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            eight_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            nine_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            zero_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            minus_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            equal_key.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);


        }
        else
        {
            l_shift.getBackground().clearColorFilter();
            r_shift.getBackground().clearColorFilter();

            wave_key.setText("`");
            forward_slash_key.setText("/");
            period_key.setText(".");
            comma_key.setText(",");
            semicolon_key.setText(";");
            apostrophe_key.setText("'");
            left_box_brac_key.setText("[");
            right_box_brac_key.setText("]");
            back_slash_key.setText("\\");
            one_key.setText("1");
            two_key.setText("2");
            three_key.setText("3");
            four_key.setText("4");
            five_key.setText("5");
            six_key.setText("6");
            seven_key.setText("7");
            eight_key.setText("8");
            nine_key.setText("9");
            zero_key.setText("0");
            minus_key.setText("-");
            equal_key.setText("=");

            wave_key.getBackground().clearColorFilter();
            forward_slash_key.getBackground().clearColorFilter();
            period_key.getBackground().clearColorFilter();
            comma_key.getBackground().clearColorFilter();
            semicolon_key.getBackground().clearColorFilter();
            apostrophe_key.getBackground().clearColorFilter();
            left_box_brac_key.getBackground().clearColorFilter();
            right_box_brac_key.getBackground().clearColorFilter();
            back_slash_key.getBackground().clearColorFilter();
            one_key.getBackground().clearColorFilter();
            two_key.getBackground().clearColorFilter();
            three_key.getBackground().clearColorFilter();
            four_key.getBackground().clearColorFilter();
            five_key.getBackground().clearColorFilter();
            six_key.getBackground().clearColorFilter();
            seven_key.getBackground().clearColorFilter();
            eight_key.getBackground().clearColorFilter();
            nine_key.getBackground().clearColorFilter();
            zero_key.getBackground().clearColorFilter();
            minus_key.getBackground().clearColorFilter();
            equal_key.getBackground().clearColorFilter();
        }
    }

    /*! \brief Describes the action to be performed when Test IP is clicked on the app.
     *
     *  This method tries to set-up a connection with the IP address entered in the textfield to check
     *  if the IP address entered by the user is valid or not.
    */
    public void test_IP(View view) {

        SERVER_IP = String.valueOf(ip_address_editText.getText());
        enter_wireless_x_button.setVisibility(View.INVISIBLE);
        SERVER_IP=SERVER_IP.trim();
        if(SERVER_IP.length() !=0)
        {
            Thread thread1 = new Thread(new TestIP_Thread());
            thread1.start();
        }
        else
        {
            ip_address_editText.setError("Please Enter Server IP");
        }

    }


    /*! \brief Tests whether the server's IP address is valid or not.
     *
     *  This method tries to set-up a connection with the server's IP address and if the address is
     *  invalid then it displays the appropriate error message.
    */
    class TestIP_Thread implements Runnable {

        public void run() {
            Socket socket;
            try {
                response="";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        test_textView.setText("Trying to Connect....");
                    }
                });
                socket = new Socket(SERVER_IP, 6666);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message= "!#Test#!";
                output.write(message);
                output.flush();

                response = input.readLine();
                output.close();
                input.close();
                socket.close();

                String s[] = response.split(" ");
                if (s.length >2)
                {
                    desktop_width = Integer.valueOf(s[1].trim());
                    desktop_height = Integer.valueOf(s[2].trim());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (response.contains("Success"))
                    {
                        enter_wireless_x_button.setVisibility(View.VISIBLE);
                        test_textView.setText("Connection Success!! "+getEmojiByUnicode(0x1F47D)+"\nEnter Wireless-X");
                        enter_wireless_x_button.setEnabled(true);
                        enter_wireless_x_button.setClickable(true);
                    }
                    else{
                        test_textView.setText("Connection Failed!! "+getEmojiByUnicode(0x1F615)+
                                "\n\n1. Recheck IP Address of Server\n" +
                                "2. Check if server is running on PC / Laptop");

                        ip_address_editText.setError("Check IP Address");
                        enter_wireless_x_button.setEnabled(false);
                        enter_wireless_x_button.setClickable(false);
                    }

                }
            });
        }
    }

    /*! \brief Returns the emoji corresponding to an unicode
    */
    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    /*! \brief Performs the action when the "Enter Wireless-X" button is clicked
     *
     *  This method displays the mouse layout once the user clicks on "Enter Wireless-X" button.
    */
    public void enter_wireless_x(View view){
        ip_address_linear_layout.setVisibility(View.GONE);
        mouse_layout.setVisibility(View.VISIBLE);
        mouse_enabled=true;

        mouse_space.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mouse_space.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mouse_space_height = mouse_space.getHeight();
                mouse_space_width=mouse_space.getWidth();
            }
        });
    }

    @Override
    /*! \brief Performs the action when the back button is pressed
     *
     *  It checks whether the back button is pressed twice within 2 seconds, if it is, then it exits the app.
     *  It also saves the IP address of the server so that the user doesn't need to re-enter it the next time he/she
     *  opens the app.
    */
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();


            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("ip_addr", String.valueOf(ip_address_editText.getText()));
            editor.commit();

            return;
        }

        this.doubleBackToExitPressedOnce = true;

        enter_wireless_x_button.setVisibility(View.INVISIBLE);
        ip_address_linear_layout.setVisibility(View.VISIBLE);
        ip_address_editText.setText(SERVER_IP);
        mouse_enabled=false;

        if (mOpenCvCameraView != null && cameraRadioButton.isChecked()) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            cameraRadioButton.setChecked(false);
            camera_switch_radio_layout.setVisibility(View.GONE);
        }
        else if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }


        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    /*! \brief Enables or disables the visibility of Mouse UI
     *
    */
    public void mouse_on_off(View view) {
        if(mouse_enabled) 
        {
            textView.setText(R.string.wireless_x_mode_keyboard);
            mouse_layout.setVisibility(View.GONE);
            keyboard_layout.setVisibility(View.VISIBLE);
        }
        else
        {
            textView.setText(R.string.wireless_x_mode_mouse);
            keyboard_layout.setVisibility(View.GONE);
            mouse_layout.setVisibility(View.VISIBLE);
            mouseRadioButton.setChecked(false);
        }

        mouse_enabled=!mouse_enabled;
    }

    /*! \brief Enables or disables the camera layout
     *
    */
    public void camera_on_off(View view) {

        if(camera_enabled) {

            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            cameraRadioButton.setChecked(false);
            camera_switch_radio_layout.setVisibility(View.GONE);
        }
        else{
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.enableView();
            camera_switch_radio_layout.setVisibility(View.VISIBLE);
        }

        camera_enabled=!camera_enabled;
    }

    /*! \brief Implementation of the camera switch button functionality
     *
     *  This method changes the main camera to the front or rear camera of the smartphone depending
     *  upon what the user has selected.
    */
    public void camera_switch(View view){

        if(mOpenCvCameraView != null)
        {
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            mOpenCvCameraView.disableView();

            if(camera_front){
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                camera_front=false;

            }
            else {
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                camera_front=true;
                cameraSwitchRadioButton.setChecked(false);
            }

            mOpenCvCameraView.enableView();
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        }
    }

    /*! \brief Checks whether all the required permissions are granted by the user or not
     *
    */
    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    @Override
    /*! \brief Sets up the camera view if all the permissions are granted
     *
     *  This method initializes all the camera parameters subject to the condition that all the required
     *  permissions are granted by the user. If this is not the case, then an error message is displayed.
    */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencvView);
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                mOpenCvCameraView.setMaxFrameSize(600, 600);
                mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
                mOpenCvCameraView.setCvCameraViewListener(this);

                if(OpenCVLoader.initDebug()){
                    mLoaderCallback.onManagerConnected(mLoaderCallback.SUCCESS);
                }
                else
                {
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this,
                            mLoaderCallback);
                }

            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }



    @Override
    /*! \brief Handles the onResume state of the app
     *
     *  If the app reaches an "onResume" state in the lifecycle, then this method checks if all the permissions 
     *  are granted or not, if they are, then it sets up camera parameters otherwise it requests the permissions.
    */
    public void onResume() {
        super.onResume();

        if(allPermissionsGranted()){
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencvView);
            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            mOpenCvCameraView.setMaxFrameSize(600, 600);
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);

            if(OpenCVLoader.initDebug()){
                mLoaderCallback.onManagerConnected(mLoaderCallback.SUCCESS);
            }
            else
            {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this,
                        mLoaderCallback);
            }

        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


        preferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String ip = preferences.getString("ip_addr", "");//"No name defined" is the default value.
        ip_address_editText.setText(ip);
        SERVER_IP=ip;
    }


    @Override
    /*! \brief Handles the onPause state of the app
     *
     *  If the app reaches the "onPause" state in the lifecycle, then this method disables the camera
     *  view. It also saves the server's IP address so that the next time the app is opened, the user
     *  doesn't require to enter the same address again.
    */
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();


        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("ip_addr", String.valueOf(ip_address_editText.getText()));
        editor.commit();

    }

    /*! \brief Handles the onDestroy state of the app
     *
     *  If the app reaches the "onDestroy" state in the lifecycle, then this method disables the camera
     *  view. 
    */
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    /// @cond DO_NOT_DOCUMENT
    public void onCameraViewStarted(int width, int height) {
        // mRGBA= new Mat(height, width, CvType.CV_8UC4);
    }
    /// @endcond

    @Override
    /// @cond DO_NOT_DOCUMENT
    public void onCameraViewStopped() {
        // mRGBA.release();
    }
    /// @endcond

    Socket soc;
    PrintWriter outToServer;
    Mat mat_t, mat;

    @Override
    /*! \brief Transmits the camera frames to the server
     *
     *  On receiving a camera frame, this method encodes that frame and transmits it to the server.
    */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mat= inputFrame.rgba();

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",mat,matOfByte);
        byte[] array= matOfByte.toArray();
        String yourString = new String(Base64.encode(array,Base64.DEFAULT));
        try {
            soc = new Socket(SERVER_IP,9998);
            outToServer = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
            outToServer.print(yourString + "#$#$#$");
            outToServer.flush();
            soc.close();
            outToServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mat;

    }

    /*! \brief Sets up the layout as defined in the "activity_main.xml" file
     *
    */
    public void layout_switch(View view) {
        setContentView(R.layout.activity_main);
    }

    /*! \brief Sends the mouse coordinates to the server
     *
     *  This method encodes the mouse coordinates such that the server is able to interpret that 
     *  the coordinates are that of mouse.
    */
    class SendMouseCoordinatesThread implements Runnable {

        float x = 0;
        float y = 0;

        public SendMouseCoordinatesThread(float X, float Y){
            x=X;
            y=Y;
        }

        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, 6666);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message= "!#Mouse#!"+x +","+y;
                output.write(message);
                output.flush();
                output.close();
                input.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*! \brief Sends the mouse clicks
     * 
    */
    public void mouse_click(View view) {
        String Key = (String) ((Button) view).getText();
        Thread thread1 = new Thread(new SendMouseClicks(Key));
        thread1.start();
    }

    /*! \brief Used to send the mouse click events to the server
     *
     *  This class implements the runnable interface, which encodes the mouse click events and sends it
     *  to the server.
    */
    class SendMouseClicks implements Runnable {

        String click_message="";

        public SendMouseClicks(String Click){
            click_message= Click;
        }

        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, 6666);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message= "!#MouseClick#!"+click_message;
                output.write(message);
                output.flush();
                output.close();
                input.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*! \brief Handles the key press event
     *
     *  This method handles the key press event and also handles the scroll button available on mouse layout.
    */
    public void keyPress(View view) {

        String Key = (String) ((Button) view).getText();

        if(Key.contains("SCROLL"))
        {
            Thread thread1 = new Thread(new SendKeyboardPressesThread(Key, "mouse"));
            thread1.start();
        }
        else
        {
            Thread thread1 = new Thread(new SendKeyboardPressesThread(Key, "keyboard"));
            thread1.start();
        }


        if(Key.equals("Caps\nLock")) {
            caps_enabled=!caps_enabled;
            if(caps_enabled)
                capslock_key.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            else
                capslock_key.getBackground().clearColorFilter();
        }


        view.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;

                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 300);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override public void run() {
                    if(Key.contains("SCROLL"))
                    {
                        Thread thread1 = new Thread(new SendKeyboardPressesThread(Key, "mouse"));
                        thread1.start();
                    }
                    else
                    {
                        Thread thread1 = new Thread(new SendKeyboardPressesThread(Key, "keyboard"));
                        thread1.start();
                    }
                    mHandler.postDelayed(this, 40);
                }
            };
        });
    }

    /*! \brief Used to send the keyboard events to the server
     *
     *  This class implements the runnable interface, which encodes the keyboard events and sends it
     *  to the server.
    */
    class SendKeyboardPressesThread implements Runnable {

        String key, device;

        public SendKeyboardPressesThread(String Key, String Device){
            key=Key;
            device=Device;
        }

        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, 6666);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message = "";
                if(device.equals("keyboard"))
                {
                    message= "!#Keyboard#!"+key;
                }
                else
                {
                    message= "!#MouseScroll#!"+key;
                }

                output.write(message);
                output.flush();
                output.close();
                input.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}