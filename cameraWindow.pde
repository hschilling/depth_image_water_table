synchronized public void cameraWindowDraw(GWinApplet appc, GWinData data) {
  //cameraWindow.setBackground(context.rgbImage());
  appc.background(context.rgbImage());

  // draw_selection_region
  appc.stroke(color(255, 0, 0));
  appc.noFill();
  appc.rect( regionMinX, regionMinY, regionMaxX - regionMinX, regionMaxY - regionMinY ) ;
}


void cameraWindowMouseEventHandler(GWinApplet appc, GWinData data, MouseEvent event){
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


