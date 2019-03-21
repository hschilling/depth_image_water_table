void setup_gui() {

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
  kinect_height = float( txtHeightKinect.getText() ) ;
  reference_water_height = float( txtReferenceWaterHeight.getText() ) ;

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
void handleButtonEvents(GButton button, GEvent event){
  if(button == btnMirrorOn && event == GEvent.CLICKED)
      context.setMirror(true);
  if(button == btnMirrorOff && event == GEvent.CLICKED)
      context.setMirror(false);
  if(button == btnCalcMinMaxDepth && event == GEvent.CLICKED)
      calcMinMaxDepth();
  if(button == btnUpdateHeight && event == GEvent.CLICKED)
    kinect_height = float( txtHeightKinect.getText() ) ;
    reference_water_height = float( txtReferenceWaterHeight.getText() ) ;
    //println( reference_water_height ) ;
    //println( kinect_height ) ;
    
  if(button == btnWriteDepthsToFile && event == GEvent.CLICKED)
      writeDepthsToFile();

  if(button == btnUserSetMinMax && event == GEvent.CLICKED)
      userSetMinMaxDepth();
      
  if(button == btnUpperHeightThreshold && event == GEvent.CLICKED)
      upperHeightThreshold = int( txtUpperHeightThreshold.getText() ) ;
      
  if(button == btnRegionSet && event == GEvent.CLICKED){
      regionMinX = int( txtRegionMinX.getText() ) ;
      regionMaxX = int( txtRegionMaxX.getText() ) ;
      regionMinY = int( txtRegionMinY.getText() ) ;
      regionMaxY = int( txtRegionMaxY.getText() ) ;
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


