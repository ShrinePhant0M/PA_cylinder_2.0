/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.lehigh.packerlab.cylinder20;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class OpenGLES20Activity extends Activity {

    private MyGLSurfaceView mGLView;
    
    private Button autoRotationButton;
    
  	private boolean isAutoRotation = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
        
        LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout autoButtonView = (LinearLayout) inflater.inflate(R.layout.layout_button,
            null, false);
		
		addContentView(autoButtonView, new LayoutParams(LayoutParams.MATCH_PARENT,
	            LayoutParams.MATCH_PARENT));
		
		autoRotationButton = (Button)findViewById(R.id.auto_rotation_button);
		autoRotationButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        // Do something in response to button click
		    	toggleRotationMode();
		    }
		});
    }
    
    private void toggleRotationMode(){
    	if(isAutoRotation){
    		isAutoRotation = false;
    		autoRotationButton.setText("Start AutoRotation");
    	}
    	else {
    		isAutoRotation = true;
    		autoRotationButton.setText("Stop AutoRotation");
    	}
    	mGLView.toggleRotationMode(isAutoRotation);
	}

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }
}