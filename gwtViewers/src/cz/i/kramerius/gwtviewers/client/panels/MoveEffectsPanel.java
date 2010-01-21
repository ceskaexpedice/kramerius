package cz.i.kramerius.gwtviewers.client.panels;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

import cz.i.kramerius.gwtviewers.client.panels.fx.Rotate;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class MoveEffectsPanel extends  Composite {
	
	private ImageRotatePool imageRotatePool;
	private ImageRotateCalculatedPositions imageRotateCalculatedPositions;
	private AbsolutePanel absolutePanel = new AbsolutePanel();
	
	private CoreConfiguration configuration;
	
	public MoveEffectsPanel( ImageMoveWrapper[] imgs, ImageMoveWrapper[] noVisibleImgs,  CoreConfiguration conf) {
		super();
		this.imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(imgs);
		this.imageRotatePool = new ImageRotatePool(imgs, noVisibleImgs);
		
		this.configuration = conf;

		this.calulateNextPositions();
		this.storeCalculatedPositions();
		
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		for (int i = 0; i < viewPortImages.size(); i++) {
			ImageMoveWrapper img = viewPortImages.get(i);
			ImageMoveWrapper calculated = this.imageRotateCalculatedPositions.getCaclulatePosition(i);
			img.setX(calculated.getX());
			img.setY(calculated.getY());
			this.absolutePanel.add(img.getWidget(), img.getX(), img.getY());
		}
		
		this.absolutePanel.setWidth(this.configuration.getViewPortWidth()+"px");
		this.absolutePanel.setHeight(this.configuration.getViewPortHeight()+"px");
		initWidget(this.absolutePanel);
	}
	

	
	public void moveLeft() {
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		boolean rollLeft = this.imageRotatePool.rollLeft();
		if (rollLeft) {
			this.calulateNextPositions();
			Rotate left = new Rotate(this.configuration, this.imageRotatePool,this.imageRotateCalculatedPositions, viewPortImages);
			left.initCompositeEffect();
			left.play();
			this.storeCalculatedPositions();
		}
	}
	
	


	public void moveRight() {
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		boolean rollRight = this.imageRotatePool.rollRight();
		if (rollRight) {
			this.calulateNextPositions();
			Rotate left = new Rotate(this.configuration, this.imageRotatePool,this.imageRotateCalculatedPositions, viewPortImages);
			left.initCompositeEffect();
			left.play();
			this.storeCalculatedPositions();
		}
	}
	



	public void calulateNextPositions() {
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		int previousImgWidth = 0;
		for (int i = 0; i < viewPortImages.size(); i++) {
			ImageMoveWrapper imageDTO = viewPortImages.get(i);
			int x =  previousImgWidth +this.configuration.getImgDistances() ;
			int y = 0;
			ImageMoveWrapper calculatedPosition = this.imageRotateCalculatedPositions.getCaclulatePosition(i);
			calculatedPosition.setX(x);
			calculatedPosition.setY(y);
			calculatedPosition.setImageIdent(imageDTO.getImageIdent());
			previousImgWidth =  x+imageDTO.getWidth() ;
		}
			
		previousImgWidth = 0;
		ArrayList<ImageMoveWrapper> leftImages = this.imageRotatePool.getLeftSideImages();
		for (int i = 0; i<leftImages.size(); i++) {
			ImageMoveWrapper imageDTO = leftImages.get(i);
			int x =  -imageDTO.getWidth() ;
			int y = 0;
			ImageMoveWrapper calculatedLeftPosition = this.imageRotateCalculatedPositions.getCaclulateLeftPosition(i);
			calculatedLeftPosition.setX(x);
			calculatedLeftPosition.setY(y);
			calculatedLeftPosition.setImageIdent(imageDTO.getImageIdent());
			previousImgWidth =  x-imageDTO.getWidth() ;
			
		}
	}
	
	public void storeCalculatedPositions() {

		
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		ArrayList<ImageMoveWrapper> calclulatedPositions = this.imageRotateCalculatedPositions.getCalclulatedPositions();
		for (int i = 0; i < viewPortImages.size(); i++) {
			ImageMoveWrapper calculatedCopy = calclulatedPositions.get(i);
			ImageMoveWrapper viewPortImage = viewPortImages.get(i);
			viewPortImage.setX(calculatedCopy.getX());
			viewPortImage.setY(calculatedCopy.getY());
		}

		ArrayList<ImageMoveWrapper> leftSide = this.imageRotatePool.getLeftSideImages();
		for (int i = leftSide.size()-1; i >= 0; i--) {
			ImageMoveWrapper viewPortImage = leftSide.get(i);
			ImageMoveWrapper calculatedCopy = this.imageRotateCalculatedPositions.getCaclulateLeftPosition(i);
			viewPortImage.setX(calculatedCopy.getX());
			viewPortImage.setY(calculatedCopy.getY());
			
		}
	}
	
	
	public void debugViewPort(String where) {
		System.out.println("--"+where);
		ArrayList<ImageMoveWrapper> imgs = this.imageRotatePool.getViewPortImages();
		for (ImageMoveWrapper img : imgs) {
			System.out.println("\t"+img.getX()+","+img.getY()+"["+img.getImageIdent()+"]");
		}
	}



	public boolean canMoveLeft() {
		return this.imageRotatePool.canRollLeft();
	}
	
	public boolean canMoveRight() {
		return this.imageRotatePool.canRollRight();
	}



	public void rollToPage(double currentValue) {
		int pocetKroku = (int)currentValue - this.imageRotatePool.getPointer();
		if (pocetKroku > 0) {
			for (int i = 0; i < pocetKroku; i++) { moveLeft(); }
		}
		if (pocetKroku < 0) {
			for (int i = pocetKroku; i < 0; i++) { moveRight(); }
		}
		//this.rollToPage(currentValue);
		
	}
}
