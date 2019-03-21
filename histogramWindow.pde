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
    minDepthSlider.setValue( 0.0 );
    maxDepthSlider = new GSlider(appc,x_begin,y_bottom +10 ,x_end - x_begin,10,10);
    maxDepthSlider.setValue( float( max_depths )  );
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
    x = int( float(i) / float(max_depths) * float( x_end - x_begin )  ) ;
    y_height = int( float( depth_histogram[i] ) / float( max_depth_histogram ) * float(y_max_height) ) ;
    appc.line( x + x_begin, y_bottom, x + x_begin, y_bottom - y_height ) ;
  }
  txtMinDepth.setText( str(0) ) ; 
  txtMaxDepth.setText( str(max_depths) ) ; 
   
   // draw min max lines
   appc.stroke( color( 255,0,0 ) );
   appc.fill( color(255,0,0) );
   x = int( x_begin + ( x_end - x_begin ) * minDepthSliderValue );
   appc.line( x, y_bottom, x, y_max_height ) ;
   
   appc.stroke( color( 0,255,0 ) );
   appc.fill( color(0,255,0) );
   x = int( x_begin + ( x_end - x_begin ) * maxDepthSliderValue );
   appc.line( x, y_bottom, x, y_max_height ) ;
   
}


void histogramWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
  
  int x, y ;
  int index ;
  int depth ;
  
  x = event.getX() ;
  y = event.getY() ;
  
  depth = int( float(x - x_begin) / (x_end - x_begin ) * max_depths ) ;
  //txtDepth.setText( str(depth) ) ; 

}

public void handleSliderEvents(GValueControl slider, GEvent event) { 
  
  if (slider == minDepthSlider) {
    minDepthSliderValue = minDepthSlider.getValueF();
    minDepthThreshold = int( max_depths * minDepthSliderValue );
    txtMinRangeDepth.setText( str(minDepthThreshold) ) ; 
  }
  else if (slider == maxDepthSlider) {
    maxDepthSliderValue = maxDepthSlider.getValueF();
    maxDepthThreshold = int( max_depths * maxDepthSliderValue );
    txtMaxRangeDepth.setText( str(maxDepthThreshold) ) ; 
  }
}



