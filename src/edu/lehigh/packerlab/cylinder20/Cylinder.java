package edu.lehigh.packerlab.cylinder20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

/**
 * This class is an object representation of 
 * a cylinder containing the vertex information,
 * texture coordinates, the vertex indices
 * and drawing functionality, which is called 
 * by the renderer.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Cylinder {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 vPosition;" +
            "attribute vec2 a_TexCoordinate;" + // Per-vertex texture coordinate information we will pass in.
            "varying vec2 v_TexCoordinate;" + // This will be passed into the fragment shader.
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vec4(vPosition, 1);" +
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform sampler2D u_Texture;" +    // The input texture.
            "varying vec2 v_TexCoordinate;" +	// Interpolated texture coordinate per fragment.
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "}";	
	
	private Context mContext;
		
	private FloatBuffer vertexBuffer;  // Vertex Buffer
	private FloatBuffer texBuffer;     // Texture Coords Buffer
	   
	private final int numFaces = 8;
	private int[] imageFileIDs = {  // Image file IDs
			R.drawable.pa_1,
			R.drawable.pa_2,
			R.drawable.pa_3,
			R.drawable.pa_4,
			R.drawable.pa_5,
			R.drawable.pa_6,
			R.drawable.pa_7,
			R.drawable.pa_8,

	};
	
	private int[] textureIDs = new int[numFaces];
	private Bitmap[] bitmap = new Bitmap[numFaces];
	
	static final int COORDS_PER_VERTEX = 3;
    private final float cylinderHalfSize =  (float) Math.tan(67.5f*Math.PI/180f);
	
    private final int mProgram;
    
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    
    private int mTextureUniformHandle;
    
    /** This will be used to pass in model texture coordinate information. */
	private int mTextureCoordinateHandle;
	
	/** Size of the texture coordinate data in elements. */
	private final int mTextureCoordinateDataSize = 2;
	
    float[] mModelMatrix = new float[16];
    float[] mMVPMatrix = new float[16];

	// Allocate texture buffer. An float has 4 bytes. Repeat for 6 faces.
	float[] texCoords = {
		   0.0f, 1.0f,  // A. left-bottom
		   1.0f, 1.0f,  // B. right-bottom
		   0.0f, 0.0f,  // C. left-top
		   1.0f, 0.0f   // D. right-top
	};
   
	// Define the vertices for this face
	float[] vertices = {
		   -1f, -0.8f, 0.0f,  // 0. left-bottom-front
		    1f, -0.8f, 0.0f,  // 1. right-bottom-front
		   -1f,  0.8f, 0.0f,  // 2. left-top-front
		    1f,  0.8f, 0.0f,  // 3. right-top-front
	};

   
	// Constructor - Set up the vertex buffer
	public Cylinder(Context context) {
		mContext = context;
	   
		// Allocate vertex buffer. An float has 4 bytes
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);  // Populate
		vertexBuffer.position(0);    // Rewind
	   
		ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		texBuffer = tbb.asFloatBuffer();
		texBuffer.put(texCoords);
		texBuffer.position(0);   // Rewind
		
		// prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
	}
	   
	// Render the shape
	public void draw(float[] vpMatrix, float rotationAngle) {
		// Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        
        // Pass in the position information
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glVertexAttribPointer(
        		mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        
        
        // Pass in the texture coordinate information
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        GLES20.glVertexAttribPointer(
        		mTextureCoordinateHandle, mTextureCoordinateDataSize,
        		GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        
        for (int face = 0; face < numFaces; face++) {
        	
            float angle=face*45f;
            
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.scaleM(mModelMatrix, 0, 0.8f, 0.8f, 0.8f);
            Matrix.rotateM(mModelMatrix, 0, angle+rotationAngle, 0, 1.0f, 0);
            Matrix.translateM(mModelMatrix, 0, 0, 0, cylinderHalfSize);
            Matrix.multiplyMM(mMVPMatrix, 0, vpMatrix, 0, mModelMatrix, 0);
            
            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            MyGLRenderer.checkGlError("glUniformMatrix4fv");
            
            //GLES20.glActiveTexture(GLES20.GL_TEXTURE);
            GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[face]);
            
            GLES20.glUniform1i(mTextureUniformHandle, 0);
            
            // Draw
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }
		
	}
	  
	// Load images into 6 GL textures
	public void loadTexture() {
		   
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
	   
		for (int face = 0; face < numFaces; face++) {
		   
			//BitmapFactory is an Android graphics utility for images
			InputStream is = mContext.getResources().openRawResource(imageFileIDs[face]);
			//imageFileIDs[face]
			bitmap[face] = BitmapFactory.decodeStream(is, null, options);
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}

		GLES20.glGenTextures(numFaces, textureIDs, 0); // Generate texture-ID array for 6 IDs

		// Generate OpenGL texture images
		for (int face = 0; face < numFaces; face++) {
			//GLES20.glActiveTexture(GLES20.GL_TEXTURE0+face);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIDs[face]);
			// Build Texture from loaded bitmap for the currently-bind texture ID
		   
			//Create Nearest Filtered Texture
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap[face], 0);
			bitmap[face].recycle();
		}
	}
}
