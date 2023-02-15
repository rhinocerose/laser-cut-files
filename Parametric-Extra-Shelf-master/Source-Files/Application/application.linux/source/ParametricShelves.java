import processing.core.*; 
import processing.xml.*; 

import proxml.*; 
import prosvg.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class ParametricShelves extends PApplet {

//SVG plugin http://sites.google.com/site/kdlprosvg/
//SVG examples http://sites.google.com/site/kdlprosvg/Home/examples

//proXML library http://creativecomputing.cc/p5libs/proxml/
// used for changing the units in the exported SVG file

//
//     THE DXF PLUGIN IS NOT WORKING FOR SOME REASON..
//

/// output DXF or SVG?
//uncomment one of these libs, and change the "outputDXF" variable accordingly
//import processing.dxf.*;


boolean outputDXF = false;
//false means we want to output SVG (we also need to uncomment the svg lib import on the top)


P5Properties props;

int canvasWidth = 400;
int canvasHeight = 400;
String unit = "mm";
String svgfile = "my_parametric_shelf.svg";
//String tempfile = "data/_TEMP.svg";
String tempfile = "data/my_parametric_shelf.svg";


public void setup(){
  if(outputDXF) size(canvasWidth, canvasHeight, P3D);
  else size(canvasWidth, canvasHeight, "prosvg.SVGKdl");
  noFill();
}

public void draw()
{
  try {
    props=new P5Properties();
    // load a configuration from a file inside the data folder
    props.load(openStream("parameters.properties"));
  }
  catch(IOException e) {
    println("couldn't read config file...");
  }
  
  /////////////////////////////////// Parameters ////
  //units in mm
  float boardThickness = props.getFloatProperty("boardThickness", 30);
  float shelfHeight = props.getFloatProperty("shelfHeight", 100);
  float shelfWidth = props.getFloatProperty("shelfWidth", 100);
  float shelfDepth = props.getFloatProperty("shelfDepth", 100);
  float shelfGapHeight = props.getFloatProperty("shelfGapHeight", 19);
  float hookHeight = props.getFloatProperty("hookHeight", 6);
  float hookLength = props.getFloatProperty("hookLength", 50);
  float numberOfBottomTeeth = props.getFloatProperty("numberOfBottomTeeth", 5);
  float numberOfSideTeeth = props.getFloatProperty("numberOfSideTeeth", 4);
  float numberOfBackTeeth = props.getFloatProperty("numberOfBackTeeth", 3);
  boolean includeFrontPlate = props.getBooleanProperty("includeFrontPlate", true);
  float frontPlateHeight = props.getFloatProperty("frontPlateHeight", 20);
  float numberOfFrontTeeth = props.getFloatProperty("numberOfFrontTeeth", 2);
  float drawingMargin = props.getFloatProperty("drawingMargin", 5);
  ///////////////////////////////////////////////////
  
  
  
  background(255);
  
  if(outputDXF) beginRaw(DXF, "my_parametric_shelf.dxf");
  
  float sideHoleSize = shelfDepth / (numberOfSideTeeth*2);
  float backHoleSize = (shelfHeight - boardThickness*2) / (numberOfBackTeeth*2);
  float bottomHoleSize = shelfWidth / (numberOfBottomTeeth*2);
  float frontHoleSize = frontPlateHeight / (numberOfFrontTeeth*2);
  float teethSlide = boardThickness;
  
  //draw sides (clockwise, starting from upper left corner)
  translate(drawingMargin, drawingMargin);
  pushMatrix();
  //the loop is used to create two of these, as both sides are the same but inverted (I invert them because some materials may have different sides
  for(int i=0; i<2; i++)
  {
    beginShape();
    vertex(shelfDepth - hookLength, 0);
    vertex(shelfDepth, 0);
    vertex(shelfDepth, shelfHeight + hookHeight + shelfGapHeight);
    vertex(0, shelfHeight + hookHeight + shelfGapHeight);
    vertex(0, hookHeight + shelfGapHeight);
    vertex(shelfDepth - hookHeight, hookHeight + shelfGapHeight);
    vertex(shelfDepth - hookHeight, hookHeight);
    vertex(shelfDepth - hookLength, hookHeight);
    endShape(CLOSE);
    
    //side holes
    pushMatrix();
    translate(sideHoleSize/2, shelfHeight + hookHeight + shelfGapHeight - boardThickness*2);
    for(int j=0; j<numberOfSideTeeth; j++)
    {
      rect(0, 0, sideHoleSize, boardThickness);
      translate(sideHoleSize*2, 0);
    }
    popMatrix();
    
    //back holes
    pushMatrix();
    translate(boardThickness*2, hookHeight + shelfGapHeight + backHoleSize/2);
    rotate(PI/2);
    for(int j=0; j<numberOfBackTeeth; j++)
    {
      rect(0, 0, backHoleSize, boardThickness);
      translate(backHoleSize*2, 0);
    }
    popMatrix();
    
    //front holes
    if(includeFrontPlate)
    {
      pushMatrix();
      translate(shelfDepth - boardThickness*2, shelfHeight + hookHeight + shelfGapHeight - boardThickness*2 - frontHoleSize/2);
      float holeY = 0;
      for(int j = 0; j<numberOfFrontTeeth; j++)
      {
        rect(0, holeY, boardThickness, -frontHoleSize);
        holeY -= frontHoleSize*2;
      }
      popMatrix();
    }
    
    //make a duplicate for the other side
    translate(2 * shelfDepth + drawingMargin, 0);
    scale(-1, 1);
  }
  popMatrix();
  
  
  //draw bottom
  pushMatrix();
  translate(0, shelfHeight + hookHeight + shelfGapHeight + drawingMargin + boardThickness*2);
  beginShape();
  vertex(0, 0);
  
  //right bottom teeth
  float teethX = (sideHoleSize/2) - teethSlide;
  for(int j=0; j<numberOfSideTeeth; j++)
  {
    vertex(teethX + teethSlide, 0);
    vertex(teethX + teethSlide, -boardThickness);
    vertex(teethX, -boardThickness);
    vertex(teethX, -boardThickness*2);
    vertex(teethX + sideHoleSize, -boardThickness*2);
    vertex(teethX + sideHoleSize, 0);
    teethX += sideHoleSize*2;
  }
  
  vertex(shelfDepth, 0);
  vertex(shelfDepth, shelfWidth);
  
  //left bottom teeth
  teethX -= sideHoleSize;
  float teethY = shelfWidth;
  for(int k=0; k<numberOfSideTeeth; k++)
  {
    vertex(teethX, teethY);
    vertex(teethX, teethY + boardThickness*2);
    vertex(teethX - sideHoleSize, teethY + boardThickness*2);
    vertex(teethX - sideHoleSize, teethY + boardThickness);
    vertex(teethX - sideHoleSize + teethSlide, teethY + boardThickness);
    vertex(teethX - sideHoleSize + teethSlide, teethY);
    teethX -= sideHoleSize*2;
  }
  
  vertex(0, shelfWidth);
  endShape(CLOSE);
  
  //back holes
  pushMatrix();
  translate(boardThickness*2, bottomHoleSize/2);
  rotate(PI/2);
  for(int j=0; j<numberOfBottomTeeth; j++)
  {
    rect(0, 0, bottomHoleSize, boardThickness);
    translate(bottomHoleSize*2, 0);
  }
  popMatrix();
  
  //keep the same matrix, to use the same Y
  //draw back
  pushMatrix();
  translate(shelfDepth + drawingMargin, 0);
  beginShape();
  vertex(0, 0);
  
  //back right teeth
  teethX = teethSlide + backHoleSize/2;
  for(int j=0; j<numberOfBackTeeth; j++)
  {
    vertex(teethX, 0);
    vertex(teethX, -boardThickness*2);
    vertex(teethX + backHoleSize, -boardThickness*2);
    vertex(teethX + backHoleSize, -boardThickness);
    vertex(teethX + backHoleSize - teethSlide, -boardThickness);
    vertex(teethX + backHoleSize - teethSlide, 0);
    teethX += backHoleSize*2;
  }
  
  vertex(shelfHeight - boardThickness*2, 0);
  
  //bottom teeth
  float teethX2 = shelfHeight - boardThickness*2;
  teethY = bottomHoleSize/2;
  for(int j=0; j<numberOfBottomTeeth; j++)
  {
    vertex(teethX2, teethY);
    vertex(teethX2 + boardThickness, teethY);
    vertex(teethX2 + boardThickness, teethY + bottomHoleSize);
    vertex(teethX2, teethY + bottomHoleSize);
    teethY += bottomHoleSize*2;
  }
  
  vertex(shelfHeight - boardThickness*2, shelfWidth);
  
  //back left teeth
  teethX -= backHoleSize;
  teethY = shelfWidth;
  for(int j=0; j<numberOfBackTeeth; j++)
  {
    vertex(teethX - teethSlide, teethY);
    vertex(teethX - teethSlide, teethY + boardThickness);
    vertex(teethX, teethY + boardThickness);
    vertex(teethX, teethY + boardThickness*2);
    vertex(teethX - backHoleSize, teethY + boardThickness*2);
    vertex(teethX - backHoleSize, teethY);
    teethX -= backHoleSize*2;
  }
  
  vertex(0, shelfWidth);
  endShape(CLOSE);
  
  
  //draw front plate
  if(includeFrontPlate)
  {
    pushMatrix();
    translate(shelfHeight - boardThickness + drawingMargin, 0);
    beginShape();
    vertex(0, 0);
    
    //right teeth
    teethX = frontHoleSize/2;
    for(int j=0; j<numberOfFrontTeeth; j++)
    {
      vertex(teethX, 0);
      vertex(teethX, -boardThickness);
      vertex(teethX + frontHoleSize, -boardThickness);
      vertex(teethX + frontHoleSize, 0);
      teethX += frontHoleSize*2;
    }
    
    vertex(frontPlateHeight, 0);
    vertex(frontPlateHeight, shelfWidth);
    
    //left teeth
    teethX = frontPlateHeight - frontHoleSize/2;
    teethY = shelfWidth;
    for(int j=0; j<numberOfFrontTeeth; j++)
    {
      vertex(teethX, teethY);
      vertex(teethX, teethY+boardThickness);
      vertex(teethX - frontHoleSize, teethY+boardThickness);
      vertex(teethX - frontHoleSize, teethY);
      teethX -= frontHoleSize*2;
    }
    
    vertex(0, shelfWidth);
    endShape(CLOSE);
    popMatrix();
  }
  
  
  popMatrix();
  popMatrix();
  
  if(outputDXF) endRaw();
  else saveFrame(tempfile);
  
  
  //after saving the SVG we need to modify it with proxml so that it displays real-world units instead of user units
  XMLInOut xmlInOut;
  xmlInOut = new XMLInOut(this);
  try{
    xmlInOut.loadElement(tempfile); 
    
  }catch(Exception e){
  }
  
    
  //Give the CPU a rest once in a while...
  delay(300);
}

public void xmlEvent(proxml.XMLElement element){
  //add units to the width and height attributes of the svg tag as well as adding the correct viewbox
  element.addAttribute("width", width + unit);
  element.addAttribute("height", height + unit);
  element.addAttribute("viewBox", "0 0 " + width + " " + height);
  XMLInOut xmlInOut;
  xmlInOut = new XMLInOut(this);
  xmlInOut.saveElement(element,svgfile);
}


/**
 * toxi's simple convenience wrapper object for the standard
 * Properties class to return pre-typed numerals
 */
class P5Properties extends Properties {
 
  public boolean getBooleanProperty(String id, boolean defState) {
    return PApplet.parseBoolean(getProperty(id,""+defState));
  }
 
  public int getIntProperty(String id, int defVal) {
    return PApplet.parseInt(getProperty(id,""+defVal)); 
  }
 
  public float getFloatProperty(String id, float defVal) {
    return PApplet.parseFloat(getProperty(id,""+defVal)); 
  }  
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "ParametricShelves" });
  }
}
