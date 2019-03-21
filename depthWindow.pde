synchronized public void depthWindowDraw(GWinApplet appc, GWinData data) {
  int     index;
  
  int delta_in_milliseconds ;
  int time_in_milliseconds ;
  
  //println( "start of depthWindowDraw" ) ;

  context.update();

  if ( last_update_time < 0.0 ) { // First time
    last_update_time = millis();
  } else {
    time_in_milliseconds = millis() ;
    delta_in_milliseconds = time_in_milliseconds - last_update_time ;
    last_update_time = time_in_milliseconds ;
    //println("delta_in_milliseconds =" + str(delta_in_milliseconds));

    if ( delta_in_milliseconds > 0.0 ) {
      frame_rate = 1000.0 / float( delta_in_milliseconds ) ;
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
        value = kinect_height - float( depth ) ; 
      } else {
        value = pow( ( kinect_height - float( depth ) ) / reference_water_height, 2 ); 
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
void depthWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
  
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
    value = kinect_height - float( depth ) ; 
  } else {
    value = pow( ( kinect_height - float( depth ) ) / reference_water_height, 2 ); 
  }
  
  txtInteractiveX.setText( str( x ) ) ;
  txtInteractiveY.setText( str( y ) ) ;
  txtInteractiveDepth.setText( str( depth ) ) ;
  txtInteractiveHeight.setText( str( kinect_height - depth ) ) ;
  txtInteractiveValue.setText( str( value ) ) ;
  //println( str(x) + "," + str(y) + "," + str(depth)) ;
    
}


