/*
   
 */

import g4p_controls.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.TextArea;

import processing.event.MouseEvent;

/* Kinect */
import SimpleOpenNI.*;
SimpleOpenNI  context;
int     step   = 3;  // to speed up the drawing of the depth points, draw every third point
int minDepth = 10000 ;
int maxDepth = 0 ;

/* the x,y coordinates of the upper left and lower right 
 corners of the selection region
 */
int regionMinX;
int regionMaxX;
int regionMinY;
int regionMaxY;

float minValue = 10000.0, maxValue = 0.0 ;

int max_depths = 4000;
int[] depth_histogram = new int[max_depths];

boolean depth_plot = true ;

float frame_rate ;
int last_update_time = -1 ;

float reference_water_height, kinect_height ;

// File I/O
PrintWriter depthsFile;

// GUI
GWindow cameraWindow, depthWindow, histogramWindow;
GButton btnMirrorOn, btnMirrorOff, btnCalcMinMaxDepth ;

GOption[] optMmessType;
GToggleGroup opgMmessType;
int md_mtype;

GOption optStep1, optStep2, optStep3, optStep4 ;
GOption optColorScale, optGreyScale ;
GOption optDepthPlot, optPressureRatioPlot ;

//GLabel lblValueMinMax, txtValueMinMax;
GLabel lblValueMinMax;
GTextField txtValueMinMax;
GLabel lblDepthMinMax;
GTextField txtDepthMinMax;

// Height variables
GLabel lblHeightKinect, lblReferenceWaterHeight;
GTextField txtHeightKinect, txtReferenceWaterHeight;
GButton btnUpdateHeight ;

// Which Min Max to use
GOption optComputedMinMax, optUserSetMinMax ;
GButton btnUserSetMinMax ;
GTextField txtUserSetMin, txtUserSetMax ;

// Upper Height Threshold
GLabel lblUpperHeightThreshold ;
GTextField txtUpperHeightThreshold ;
GButton btnUpperHeightThreshold ;
int upperHeightThreshold = 10000;

// Upper Height Threshold
GLabel lblRegionMinX,lblRegionMaxX,lblRegionMinY,lblRegionMaxY  ;
GTextField txtRegionMinX,txtRegionMaxX,txtRegionMinY,txtRegionMaxY ;
GButton btnRegionSet ;

// Frame rate
GLabel lblFrameRate;
GTextField txtFrameRate;

// Write to file
GButton btnWriteDepthsToFile ;
GTextField txtDepthsFileName;

// Interactive values
GTextField txtInteractiveX, txtInteractiveY, txtInteractiveDepth, txtInteractiveHeight, txtInteractiveValue ;

/* array of colors for the color map */
color[] colors=new color[256];

void setup() {
  size(500, 1000); // main GUI window

  setup_openni() ;
  setup_gui() ;
  setup_windows() ;
  load_color_scale() ;
}

void setup_openni() {
  int depthHeight ;
  int depthWidth ;
  context = new SimpleOpenNI(this);

  // mirror is by default enabled
  context.setMirror(false);

  // enable depthMap generation 
  if (context.enableDepth() == false)
  {
    println("Can't open the depthMap, maybe the camera is not connected!"); 
    exit();
    return;
  }
  if (context.enableRGB() == false)
  {
    println("Can't open the rgbMap, maybe the camera is not connected or there is no rgbSensor!"); 
    exit();
    return;
  }
  
    // try and keep it at 30fps
  frameRate(30);
  
  calcMinMaxDepth() ;
  
  depthHeight = context.depthHeight() ;
  depthWidth = context.depthWidth() ;
  regionMinX = 0 ;
  regionMaxX = depthWidth - 1 ;
  regionMinY = 0 ;
  regionMaxY = depthHeight - 1 ;
  
}

void setup_windows() {
  // last param can be any of JAVA2D / P2D / P3D / OPENGL
  // JAVA2D better than P2D and P3D in terms of frame rate. OPENGL is good too
  cameraWindow = new GWindow(this, "Camera", 50, 100, context.rgbWidth() , context.rgbHeight() , false, OPENGL);
  cameraWindow.addDrawHandler(this, "cameraWindowDraw");
  cameraWindow.addMouseHandler(this, "cameraWindowMouseEventHandler");

  depthWindow = new GWindow(this, "Depth", 1200, 100, context.depthWidth(), context.depthHeight(), false, JAVA2D);
  depthWindow.addDrawHandler(this, "depthWindowDraw");
  depthWindow.addMouseHandler(this, "depthWindowMouseEventHandler");
  //depthWindow.setBackground(Color.RED); 

  histogramWindow = new GWindow(this, "Histogram", 1200, 900, 500, 300, false, JAVA2D);
  histogramWindow.addDrawHandler(this, "histogramWindowDraw");
  histogramWindow.addMouseHandler(this, "histogramWindowMouseEventHandler");
  histogramWindow.addPreHandler(this, "histogramWindowPreHandler");


}




void writeDepthsToFile() {
  int[]   depthMap = context.depthMap();
  int depthHeight = context.depthHeight() ;
  int depthWidth = context.depthWidth() ;
  int index ;
  int depth;

  depthsFile = createWriter(txtDepthsFileName.getText()); 
  depthsFile.println("X,Y,depth_in_mm");
  for (int y=0;y < depthHeight;y+=step) {
    for (int x=0;x < depthWidth;x+=step) {
      index = x + y * depthWidth;
      depth = depthMap[index] ;
      depthsFile.println( str(x) + "," + str(y) + "," + str(depth)) ;
    }
  }
  depthsFile.flush();  // Writes the remaining data to the file
  depthsFile.close();  // Finishes the file
}




void userSetMinMaxDepth() {
 minValue =  float( txtUserSetMin.getText() ) ;
 maxValue =  float( txtUserSetMax.getText() ) ;
}

void calcMinMaxDepth() {
  int[]   depthMap = context.depthMap();
  int depthHeight = context.depthHeight() ;
  int depthWidth = context.depthWidth() ;
  int depth, h ;
  int index ;
  float value ;
  
  for (int i=0; i < max_depths; i++) {         
    depth_histogram[i] = 0 ;
  }

 
  minDepth = 10000 ;
  maxDepth = 0 ;
  
  println( "minDepthThreshold = " + str(minDepthThreshold ) );
  println( "maxDepthThreshold = " + str(maxDepthThreshold ) );
  
  minValue = 10000.0 ;
  maxValue = 0.0 ;
  for (int y=regionMinY;y <= regionMaxY;y+=step) {
    for (int x=regionMinX;x <= regionMaxX;x+=step) {
      index = x + y * depthWidth;
      depth = depthMap[index] ;
      if ( depth_plot ) {
        value = kinect_height - float( depth ) ; 
      } else {
        value = pow( ( kinect_height - float( depth ) ) / reference_water_height, 2 ); 
      }
      //if ( depth > 0 && ( ( kinect_height - depth ) < upperHeightThreshold ) ) {
        if ( x == regionMinX && y == regionMinY ) println( "depth = " + depth ) ;
      //if ( depth > 0 && ( depth > minDepthThreshold ) && ( depth < maxDepthThreshold ) ) {
      if ( depth > 0 ) {
        if ( value < minValue ) minValue = value ;
        if ( value > maxValue ) maxValue = value ;
      }
      if ( depth > 0 ) {
        h = int( kinect_height ) - depth ;
        depth_histogram[ h ] += 1 ;
        //println( str(h) + ":" + str( depth_histogram[ h ] ) ) ;
        if ( depth < minDepth ) minDepth = depth ;
        if ( depth > maxDepth ) maxDepth = depth ;
    }
  }
  }
  println( "min max value  " + str( minValue ) + "   " + str( maxValue ) ) ;
}

 

/**
 * Draw for the main window
 */
void draw() {
  //background(240);
  
  
  //context.update();
  
  
  //println("update");
  //cameraWindow.setBackground(context.rgbImage());
  //image(context.rgbImage(), 0, 0);
  
  // write minDepth and maxDepth
  //println( minValue ) ;
  //println( maxValue ) ;
  
  //txtValueMinMax.appendText( str( minValue ) + " to " + str( maxValue ) ) ;
  //txtValueMinMax.appendText( str( minDepth ) + " to " + str( maxDepth ) ) ;
  txtDepthMinMax.setText( str( minDepth ) + " to " + str( maxDepth ) ) ;
  txtValueMinMax.setText( str( minValue ) + " to " + str( maxValue ) ) ;
  //txtValueMinMax.setText( str( minDepth )  ) ;
  //txtValueMinMax.setText( "gleep" ) ;
  //txtValueMinMax.setText( str( minDepth ) ) ;
  
  //draw_depth_histogram() ;

}


///**
//Create the three windows so that they share mouse handling 
//and drawing code.
//*/
//void createWindows() {
//  int col;
//  window = new GWindow[3];
//  for (int i = 0; i < 3; i++) {
//    col = (128 << (i * 8)) | 0xff000000;
//    window[i] = new GWindow(this, "Window "+i, 70+i*220, 160+i*50, 200, 200, false, JAVA2D);
//    window[i].setBackground(col);
//    window[i].addData(new MyWinData());
//    window[i].addDrawHandler(this, "windowDraw");
//    window[i].addMouseHandler(this, "windowMouse");
//  }
//}
//
///**
// * Click the button to create the windows.
// * @param button
// */
//void handleButtonEvents(GButton button, GEvent event) {
//  if (window == null && event == GEvent.CLICKED) {
//    createWindows();
//    lblInstr.setVisible(true);
//    button.setEnabled(false);
//  }
//}
//
///**
// * Handles mouse events for ALL GWindow objects
// *  
// * @param appc the PApplet object embeded into the frame
// * @param data the data for the GWindow being used
// * @param event the mouse event
// */
//void windowMouse(GWinApplet appc, GWinData data, MouseEvent event) {
//  MyWinData data2 = (MyWinData)data;
//  switch(event.getAction()) {
//  case MouseEvent.PRESS:
//    data2.sx = data2.ex = appc.mouseX;
//    data2.sy = data2.ey = appc.mouseY;
//    data2.done = false;
//    break;
//  case MouseEvent.RELEASE:
//    data2.ex = appc.mouseX;
//    data2.ey = appc.mouseY;
//    data2.done = true;
//    break;
//  case MouseEvent.DRAG:
//    data2.ex = appc.mouseX;
//    data2.ey = appc.mouseY;
//    break;
//  }
//}
//
///**
// * Handles drawing to the windows PApplet area
// * 
// * @param appc the PApplet object embeded into the frame
// * @param data the data for the GWindow being used
// */
//void windowDraw(GWinApplet appc, GWinData data) {
//  MyWinData data2 = (MyWinData)data;
//  if (!(data2.sx == data2.ex && data2.ey == data2.ey)) {
//    appc.stroke(255);
//    appc.strokeWeight(2);
//    appc.noFill();
//    if (data2.done) {
//      appc.fill(128);
//    }
//    appc.rectMode(CORNERS);
//    appc.rect(data2.sx, data2.sy, data2.ex, data2.ey);
//  }
//}  
//
///**
// * Simple class that extends GWinData and holds the data 
// * that is specific to a particular window.
// * 
// * @author Peter Lager
// */
//class MyWinData extends GWinData {
//  int sx, sy, ex, ey;
//  boolean done;
//}
