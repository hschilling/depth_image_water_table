void load_grey_scale() {
    for (int i = 0; i < 256; i++) {
      colors[i] = color( float(i), float(i), float(i)) ; 
    }
}

void load_color_scale() {
  String[] pieces ;
  String[] lines;

  //lines = loadStrings("ncl_default.rgb");
  lines = loadStrings("GMT_seis.rgb");
  for (int i = 0; i < lines.length; i++) {
    if ( i > 2 ) {
      pieces = splitTokens(lines[i], " "); // Load data into array
      colors[255-(i-3)] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ; 
      //colors[i-3] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ; 
//      if ( i == lines.length - 1 ) { // this color map only has 254 entries
//        colors[255-(i-2)] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ; 
//        colors[255-(i-1)] = color( float(pieces[0])*255, float(pieces[1])*255, float(pieces[2]) *255 ) ;
//      }
    }
  }
}


void draw_color_scale() {
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

