import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import java.awt.Color; 
import java.awt.Font; 
import java.awt.TextArea; 
import processing.event.MouseEvent; 
import SimpleOpenNI.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class DepthImage_water_table_v10 extends PApplet {

/*
   
 */









/* Kinect */

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

float minValue = 10000.0f, maxValue = 0.0f ;

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
int[] colors=new int[256];

public void setup() {
  size(500, 1000); // main GUI window

  setup_openni() ;
  setup_gui() ;
  setup_windows() ;
  load_color_scale() ;
}

public void setup_openni() {
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

public void setup_windows() {
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




public void writeDepthsToFile() {
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




public void userSetMinMaxDepth() {
 minValue =  PApplet.parseFloat( txtUserSetMin.getText() ) ;
 maxValue =  PApplet.parseFloat( txtUserSetMax.getText() ) ;
}

public void calcMinMaxDepth() {
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
  
  minValue = 10000.0f ;
  maxValue = 0.0f ;
  for (int y=regionMinY;y <= regionMaxY;y+=step) {
    for (int x=regionMinX;x <= regionMaxX;x+=step) {
      index = x + y * depthWidth;
      depth = depthMap[index] ;
      if ( depth_plot ) {
        value = kinect_height - PApplet.parseFloat( depth ) ; 
      } else {
        value = pow( ( kinect_height - PApplet.parseFloat( depth ) ) / reference_water_height, 2 ); 
      }
      //if ( depth > 0 && ( ( kinect_height - depth ) < upperHeightThreshold ) ) {
        if ( x == regionMinX && y == regionMinY ) println( "depth = " + depth ) ;
      //if ( depth > 0 && ( depth > minDepthThreshold ) && ( depth < maxDepthThreshold ) ) {
      if ( depth > 0 ) {
        if ( value < minValue ) minValue = value ;
        if ( value > maxValue ) maxValue = value ;
      }
      if ( depth > 0 ) {
        h = PApplet.parseInt( kinect_height ) - depth ;
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
public void draw() {
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
synchronized public void cameraWindowDraw(GWinApplet appc, GWinData data) {
  //cameraWindow.setBackground(context.rgbImage());
  appc.background(context.rgbImage());

  // draw_selection_region
  appc.stroke(color(255, 0, 0));
  appc.noFill();
  appc.rect( regionMinX, regionMinY, regionMaxX - regionMinX, regionMaxY - regionMinY ) ;
}


public void cameraWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
  int x, y ;

    switch(event.getAction()){
      case MouseEvent.PRESS:
        x = event.getX() ;
        y = event.getY() ;
        regionMinX = x ;
        regionMinY = y ;
        break;
      case MouseEvent.RELEASE:
        x = event.getX() ;
        y = event.getY() ;
        regionMaxX = x ;
        regionMaxY = y ;
        break;
      case MouseEvent.CLICK:
        break;
      case MouseEvent.DRAG:
        x = event.getX() ;
        y = event.getY() ;
        regionMaxX = x ;
        regionMaxY = y ;
        break;
      case MouseEvent.MOVE:
        break;
    }
}


public void load_grey_scale() {
    for (int i = 0; i < 256; i++) {
      colors[i] = color( PApplet.parseFloat(i), PApplet.parseFloat(i), PApplet.parseFloat(i)) ; 
    }
}

public void load_color_scale() {
  String[] pieces ;
  String[] lines;

  //lines = loadStrings("ncl_default.rgb");
  lines = loadStrings("GMT_seis.rgb");
  for (int i = 0; i < lines.length; i++) {
    if ( i > 2 ) {
      pieces = splitTokens(lines[i], " "); // Load data into array
      colors[255-(i-3)] = color( PApplet.parseFloat(pieces[0])*255, PApplet.parseFloat(pieces[1])*255, PApplet.parseFloat(pieces[2]) *255 ) ; 
      //colors[i-3] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ; 
//      if ( i == lines.length - 1 ) { // this color map only has 254 entries
//        colors[255-(i-2)] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ; 
//        colors[255-(i-1)] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ;
//      }
    }
  }
}


public void draw_color_scale() {
  int x, y;
  int colorScaleWidth = 30 ;

  x = 370 ;
    for (int i=0;i < 256;i++) {
        y = 10 + 256 - i;
        stroke(colors[i]);
        fill(colors[i] );
        line(x,y,x+colorScaleWidth,y);
    }
}

synchronized public void depthWindowDraw(GWinApplet appc, GWinData data) {
  int     index;
  
  int delta_in_milliseconds ;
  int time_in_milliseconds ;
  
  //println( "start of depthWindowDraw" ) ;

  context.update();

  if ( last_update_time < 0.0f ) { // First time
    last_update_time = millis();
  } else {
    time_in_milliseconds = millis() ;
    delta_in_milliseconds = time_in_milliseconds - last_update_time ;
    last_update_time = time_in_milliseconds ;
    //println("delta_in_milliseconds =" + str(delta_in_milliseconds));

    if ( delta_in_milliseconds > 0.0f ) {
      frame_rate = 1000.0f / PApplet.parseFloat( delta_in_milliseconds ) ;
      txtFrameRate.setText( str( frame_rate ) ) ; 
    } else {
      txtFrameRate.setText( "infinity" ) ; 
    }
    //println("after settext" ) ;
  }



  //An array containing all of the distances in millimetres
  int[]   depthMap = context.depthMap();
  int depth ;
  int levelColor ;
  
  float value ;

  int depthHeight = context.depthHeight() ;
  int depthWidth = context.depthWidth() ;
//  appc.background(100,100,200);
//  appc.fill(0,0,160);
//  appc.noStroke();
//  appc.ellipse(appc.width/2, appc.height/2, appc.width/1.2, appc.height/1.2); 
//  appc.fill(255);
//  appc.text("Secondary window", 20, 20);
  
//So, we will have to record the height of the kinect, hk, with respect 
// to the water table glass floor as well. Then we find the reference 
// water height, hw. Measurement of surface distance from Kinect is hs and obtained from the image.
//Therefore, the pressure ratio is [(hk-hs)/(hw)]^2. 

//depthHeight = 200 ;
//depthWidth = 200 ;

  for (int y=regionMinY;y <= regionMaxY;y+=step) {
  for (int x=regionMinX;x <= regionMaxX;x+=step) {
    index = x + y * depthWidth;
    depth = depthMap[index] ;
    if ( depth > 0 && ( ( kinect_height - depth ) < upperHeightThreshold ) ) {
      //if ( depth < minDepth ) minDepth = depth ;
      //if ( depth > maxDepth ) maxDepth = depth ;
      
      depth = depthMap[index] ;
      if ( depth_plot ) {
        value = kinect_height - PApplet.parseFloat( depth ) ; 
      } else {
        value = pow( ( kinect_height - PApplet.parseFloat( depth ) ) / reference_water_height, 2 ); 
      }

      /* Draw the depth value as a color */
      //levelColor   = (int) map( depth, minDepth, maxDepth, 0, 255 );
      levelColor   = (int) map( value, minValue, maxValue, 0, 255 );
      //levelColor   = (int) map( value, minValue, maxValue, 255, 0 );
      if ( levelColor < 0 ) {
        levelColor = 0 ;
      }
      if (levelColor > 255 ) {
        levelColor = 255 ;
      }
      appc.stroke(colors[levelColor]);
      appc.fill(colors[levelColor] );
      //point(x,y);
      appc.ellipse(x, y, 4, 4) ; // use a fat point to make up for the fact that only drawing every third point
    }
    else {
    }
  }
  }
  //println( "end of depthWindowDraw" ) ;

}
public void depthWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
  
  int x, y ;
  int index ;
  int depth ;
  float value ;
  int depthWidth = context.depthWidth() ;
  int depthHeight = context.depthHeight() ;
  int[]   depthMap = context.depthMap();
  
  x = event.getX() ;
  if ( x < 0 ) x = 0 ;
  if ( x >= depthWidth ) x = depthWidth - 1 ; 
  y = event.getY() ;
  if ( y < 0 ) y = 0 ;
  if ( y >= depthHeight ) y = depthHeight - 1 ; 
  
  index = x + y * depthWidth;
  depth = depthMap[index] ;
  if ( depth_plot ) {
    value = kinect_height - PApplet.parseFloat( depth ) ; 
  } else {
    value = pow( ( kinect_height - PApplet.parseFloat( depth ) ) / reference_water_height, 2 ); 
  }
  
  txtInteractiveX.setText( str( x ) ) ;
  txtInteractiveY.setText( str( y ) ) ;
  txtInteractiveDepth.setText( str( depth ) ) ;
  txtInteractiveHeight.setText( str( kinect_height - depth ) ) ;
  txtInteractiveValue.setText( str( value ) ) ;
  //println( str(x) + "," + str(y) + "," + str(depth)) ;
    
}


public void setup_gui() {

  // Mirroring
  btnMirrorOn = new GButton(this, 10, 10, 100, 20, "Mirror On");
  btnMirrorOff = new GButton(this, 120, 10, 100, 20, "Mirror Off");
  
  int x,y ;
  int xData = 200;
  x = 0 ;
  y = 30 ;

  // Step size
  GToggleGroup tgStep = new GToggleGroup();
  GLabel lblTestStyle ;
 
  lblTestStyle = new GLabel(this, x, y, 80, 18, "Step Size");
  lblTestStyle.setTextBold();
  optStep1 = new GOption(this, x, y + 20, 80, 18, "1");
  optStep2 = new GOption(this, x, y + 40, 80, 18, "2");
  optStep3 = new GOption(this, x, y + 60, 80, 18, "3");
  optStep4 = new GOption(this, x, y + 80, 80, 18, "4");
  tgStep.addControls(optStep1, optStep2, optStep3, optStep4);
  optStep3.setSelected(true);
    
  // Color map
  GToggleGroup tgColorMap = new GToggleGroup();
  GLabel lblColorMap ;
  lblColorMap = new GLabel(this, x, y + 120, 80, 18, "Color Map");
  lblColorMap.setTextBold();
  optGreyScale = new GOption(this, x, y + 140, 80, 18, "grey");
  optColorScale = new GOption(this, x, y + 160, 80, 18, "color");
  tgColorMap.addControls(optGreyScale, optColorScale);
  optColorScale.setSelected(true);

  // Calc min max button
  btnCalcMinMaxDepth = new GButton(this, x, y + 200 , 160, 20, "Calc Min Max Depth");

  // Height reference variables
  lblHeightKinect = new GLabel(this, x,  y + 240, 140, 18, "Kinect Height (mm)" );
  lblHeightKinect.setTextBold();
  txtHeightKinect = new GTextField(this, x,  y + 260, 160, 18 );
  txtHeightKinect.setText( "4000.0" ) ;

  lblReferenceWaterHeight = new GLabel(this, x,  y + 300, 200, 18, "Reference Water Height (mm)" );
  lblReferenceWaterHeight.setTextBold();
  txtReferenceWaterHeight = new GTextField(this, x,  y + 320, 160, 18 );
  txtReferenceWaterHeight.setText( "100.0" ) ;
  btnUpdateHeight = new GButton(this, x,   y + 360, 100, 20, "Update Heights");
  kinect_height = PApplet.parseFloat( txtHeightKinect.getText() ) ;
  reference_water_height = PApplet.parseFloat( txtReferenceWaterHeight.getText() ) ;

  // Plot depth or pressure ratio?
  GToggleGroup tgPlotValue = new GToggleGroup();
  GLabel lblPlotValue ;
  lblPlotValue = new GLabel(this, x, y+400, 80, 18, "Plot Value");
  lblPlotValue.setTextBold();
  optDepthPlot = new GOption(this, x, y + 420, 80, 18, "Height");
  optPressureRatioPlot = new GOption(this, x, y + 440, 140, 18, "Pressure Ratio");
  tgPlotValue.addControls(optDepthPlot, optPressureRatioPlot);
  optDepthPlot.setSelected(true);

   // Write depths to file
   btnWriteDepthsToFile = new GButton(this, x, y + 480, 140, 18, "Write Depths to File");
   txtDepthsFileName = new GTextField(this, x,  y + 510, 160, 18 );
   txtDepthsFileName.setText( "depths.csv" ) ;
   
   // Upper Height Threshold
   btnUpperHeightThreshold = new GButton(this, x,   y + 680, 150, 20, "Upper Height Threshold");
   lblUpperHeightThreshold = new GLabel(this, x, y+710, 40, 18, "ht th");
   lblUpperHeightThreshold.setTextBold();
   txtUpperHeightThreshold = new GTextField(this, x + 40 ,  y + 710, 160, 18 );
   txtUpperHeightThreshold.setText( str( upperHeightThreshold ) ) ;

  // Region Set
   btnRegionSet = new GButton(this, x,   y + 750, 150, 20, "Set Region");
   lblRegionMinX = new GLabel(this, x, y+770, 40, 18, "MinX");
   lblRegionMinX.setTextBold();
   txtRegionMinX = new GTextField(this, x + 40 ,  y + 770, 160, 18 );
   txtRegionMinX.setText( str( regionMinX ) ) ;
  
   lblRegionMaxX = new GLabel(this, x, y+790, 40, 18, "MaxX");
   lblRegionMaxX.setTextBold();
   txtRegionMaxX = new GTextField(this, x + 40 ,  y + 790, 160, 18 );
   txtRegionMaxX.setText( str( regionMaxX ) ) ;
  
   lblRegionMinY = new GLabel(this, x, y+810, 40, 18, "MinY");
   lblRegionMinY.setTextBold();
   txtRegionMinY = new GTextField(this, x + 40 ,  y + 810, 160, 18 );
   txtRegionMinY.setText( str( regionMinY ) ) ;
  
   lblRegionMaxY = new GLabel(this, x, y+830, 40, 18, "MaxY");
   lblRegionMaxY.setTextBold();
   txtRegionMaxY = new GTextField(this, x + 40 ,  y + 830, 160, 18 );
   txtRegionMaxY.setText( str( regionMaxY ) ) ;
  
   
    // Plot depth or pressure ratio?
//    GToggleGroup tgMinMaxChoice = new GToggleGroup();
//    GLabel lblValueMinMaxChoice ;
//    lblValueMinMaxChoice = new GLabel(this, x, y+550,120, 18, "Which Min Max?");
//    lblValueMinMaxChoice.setTextBold();
//    optComputedMinMax = new GOption(this, x, y + 570, 80, 18, "Computed");
//    optUserSetMinMax = new GOption(this, x, y + 590, 80, 18, "User Set");
//    tgMinMaxChoice.addControls(optComputedMinMax, optUserSetMinMax);
//    optComputedMinMax.setSelected(true);

    btnUserSetMinMax = new GButton(this, x, y + 570, 140, 18, "Use User Set Min Max");
    GLabel lblUserSetMin, lblUserSetMax;
    lblUserSetMin = new GLabel(this, x, y+610,30, 18, "Min");
    lblUserSetMin.setTextBold();
    lblUserSetMax = new GLabel(this, x, y+630,30, 18, "Max");
    lblUserSetMax.setTextBold();
    txtUserSetMin = new GTextField(this, x + 40,  y + 610, 160, 18 );
    txtUserSetMax = new GTextField(this, x + 40,  y + 630, 160, 18 );

    ///////// Outputs /////////
    // updated min and max values
    lblDepthMinMax = new GLabel(this, xData,  y + 320, 140, 18, "Depth min max" );
    lblDepthMinMax.setTextBold();
    txtDepthMinMax = new GTextField(this, xData,  y + 350, 160, 18 );
    
    lblValueMinMax = new GLabel(this, xData,  y + 420, 140, 18, "Value min max" );
    lblValueMinMax.setTextBold();
    txtValueMinMax = new GTextField(this, xData,  y + 450, 160, 18 );
    //txtValueMinMax = new GTextArea(this, xData,  y + 50, 160, 100, TextArea.SCROLLBARS_VERTICAL_ONLY );
    //txtValueMinMax.setFont(new Font("Dialog", Font.PLAIN, 14));
    
    // frame rate
    lblFrameRate = new GLabel(this, xData,  y + 220, 140, 18, "Frame Rate" );
    lblFrameRate.setTextBold();
    txtFrameRate = new GTextField(this, xData,  y + 240, 140, 18);
    
    // interactive values of X,Y, Depth, Height, and Value
    GLabel lblInteractiveX = new GLabel(this, xData, y+610,50, 18, "X");
    lblInteractiveX.setTextBold();
    txtInteractiveX = new GTextField(this, xData + 50,  y + 610, 160, 18 );
    
    GLabel lblInteractiveY = new GLabel(this, xData, y+630,50, 18, "Y");
    lblInteractiveY.setTextBold();
    txtInteractiveY = new GTextField(this, xData + 50,  y + 630, 160, 18 );
    
    GLabel lblInteractiveDepth = new GLabel(this, xData, y+650,50, 18, "Depth");
    lblInteractiveDepth.setTextBold();
    txtInteractiveDepth = new GTextField(this, xData + 50,  y + 650, 160, 18 );
    
    GLabel lblInteractiveHeight = new GLabel(this, xData, y+670,50, 18, "Height");
    lblInteractiveHeight.setTextBold();
    txtInteractiveHeight = new GTextField(this, xData + 50,  y + 670, 160, 18 );
    
    GLabel lblInteractiveValue = new GLabel(this, xData, y+690,50, 18, "Value");
    lblInteractiveValue.setTextBold();
    txtInteractiveValue = new GTextField(this, xData + 50,  y + 690, 160, 18 );

    draw_color_scale() ;
    
}
public void handleTextEvents(GEditableTextControl textarea, GEvent event) {
  /* nothing to do but this prevents warning messages */
}


// This method is called when a button is clicked
public void handleButtonEvents(GButton button, GEvent event){
  if(button == btnMirrorOn && event == GEvent.CLICKED)
      context.setMirror(true);
  if(button == btnMirrorOff && event == GEvent.CLICKED)
      context.setMirror(false);
  if(button == btnCalcMinMaxDepth && event == GEvent.CLICKED)
      calcMinMaxDepth();
  if(button == btnUpdateHeight && event == GEvent.CLICKED)
    kinect_height = PApplet.parseFloat( txtHeightKinect.getText() ) ;
    reference_water_height = PApplet.parseFloat( txtReferenceWaterHeight.getText() ) ;
    //println( reference_water_height ) ;
    //println( kinect_height ) ;
    
  if(button == btnWriteDepthsToFile && event == GEvent.CLICKED)
      writeDepthsToFile();

  if(button == btnUserSetMinMax && event == GEvent.CLICKED)
      userSetMinMaxDepth();
      
  if(button == btnUpperHeightThreshold && event == GEvent.CLICKED)
      upperHeightThreshold = PApplet.parseInt( txtUpperHeightThreshold.getText() ) ;
      
  if(button == btnRegionSet && event == GEvent.CLICKED){
      regionMinX = PApplet.parseInt( txtRegionMinX.getText() ) ;
      regionMaxX = PApplet.parseInt( txtRegionMaxX.getText() ) ;
      regionMinY = PApplet.parseInt( txtRegionMinY.getText() ) ;
      regionMaxY = PApplet.parseInt( txtRegionMaxY.getText() ) ;
  }
      
    
}

// Called when a toggle button event happens
public void handleToggleControlEvents(GToggleControl option, GEvent event) {
  if (option == optStep1)
    step = 1 ;
  else if (option == optStep2)
    step = 2 ;
  else if (option == optStep3)
    step = 3 ;
  else if (option == optStep4)
    step = 4 ;
    
  if (option == optColorScale) {
    load_color_scale() ;
    draw_color_scale() ;
  } else if (option == optGreyScale) {
    load_grey_scale() ;
    draw_color_scale() ;
  }
   
  if (option == optDepthPlot )
     depth_plot = true ;
  else if (option == optPressureRatioPlot )
     depth_plot = false ;

  if (option == optComputedMinMax )
     depth_plot = true ;
  else if (option == optUserSetMinMax )
     depth_plot = false ;


}


GTextField txtMinDepth, txtMaxDepth, txtMinRangeDepth, txtMaxRangeDepth ;

GSlider minDepthSlider, maxDepthSlider ;

int minDepthThreshold, maxDepthThreshold ; 


int y_bottom = 250 ;
int y_max_height = 50 ;
int x_begin = 40;
int x_end =  480 ;
int txt_width = 40 ;
boolean gui_elements_drawn = false;

float minDepthSliderValue, maxDepthSliderValue ;

synchronized public void histogramWindowPreHandler(GWinApplet appc, GWinData data) {
  if ( ! gui_elements_drawn ) {
    txtMinDepth = new GTextField(appc, x_begin - ( txt_width /2 ),  y_bottom + 30, txt_width, 18 );
    txtMaxDepth = new GTextField(appc, x_end - ( txt_width/2 ),  y_bottom + 30, txt_width, 18 );
    txtMinRangeDepth = new GTextField(appc, x_begin + ( x_end - x_begin ) / 4,  y_bottom + 30, txt_width, 18 );
    txtMaxRangeDepth = new GTextField(appc, x_begin + 3 * ( x_end - x_begin ) / 4,  y_bottom + 30, txt_width, 18 );
    // last parameter is the slider track thickness
    minDepthSlider = new GSlider(appc,x_begin,y_bottom +0 ,x_end - x_begin,10,10);
    minDepthSlider.setValue( 0.0f );
    maxDepthSlider = new GSlider(appc,x_begin,y_bottom +10 ,x_end - x_begin,10,10);
    maxDepthSlider.setValue( PApplet.parseFloat( max_depths )  );
    gui_elements_drawn = true ;
  }
}

synchronized public void histogramWindowDraw(GWinApplet appc, GWinData data) {
  int y_height ;
  int x ;
  int max_depth_histogram = 0 ;
  
  appc.stroke( color( 0,0,0 ) );
  appc.fill( color(0,0,0) );
  
  // get the height of the highest bar in the histogram so we can scale the plot
   for (int i=0;i < max_depths;i++) {
    if ( depth_histogram[i] > max_depth_histogram ) max_depth_histogram = depth_histogram[i] ;
  }
 
  for (int i=0;i < max_depths;i++) {
    x = PApplet.parseInt( PApplet.parseFloat(i) / PApplet.parseFloat(max_depths) * PApplet.parseFloat( x_end - x_begin )  ) ;
    y_height = PApplet.parseInt( PApplet.parseFloat( depth_histogram[i] ) / PApplet.parseFloat( max_depth_histogram ) * PApplet.parseFloat(y_max_height) ) ;
    appc.line( x + x_begin, y_bottom, x + x_begin, y_bottom - y_height ) ;
  }
  txtMinDepth.setText( str(0) ) ; 
  txtMaxDepth.setText( str(max_depths) ) ; 
   
   // draw min max lines
   appc.stroke( color( 255,0,0 ) );
   appc.fill( color(255,0,0) );
   x = PApplet.parseInt( x_begin + ( x_end - x_begin ) * minDepthSliderValue );
   appc.line( x, y_bottom, x, y_max_height ) ;
   
   appc.stroke( color( 0,255,0 ) );
   appc.fill( color(0,255,0) );
   x = PApplet.parseInt( x_begin + ( x_end - x_begin ) * maxDepthSliderValue );
   appc.line( x, y_bottom, x, y_max_height ) ;
   
}


public void histogramWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
  
  int x, y ;
  int index ;
  int depth ;
  
  x = event.getX() ;
  y = event.getY() ;
  
  depth = PApplet.parseInt( PApplet.parseFloat(x - x_begin) / (x_end - x_begin ) * max_depths ) ;
  //txtDepth.setText( str(depth) ) ; 

}

public void handleSliderEvents(GValueControl slider, GEvent event) { 
  
  if (slider == minDepthSlider) {
    minDepthSliderValue = minDepthSlider.getValueF();
    minDepthThreshold = PApplet.parseInt( max_depths * minDepthSliderValue );
    txtMinRangeDepth.setText( str(minDepthThreshold) ) ; 
  }
  else if (slider == maxDepthSlider) {
    maxDepthSliderValue = maxDepthSlider.getValueF();
    maxDepthThreshold = PApplet.parseInt( max_depths * maxDepthSliderValue );
    txtMaxRangeDepth.setText( str(maxDepthThreshold) ) ; 
  }
}



  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "DepthImage_water_table_v10" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
