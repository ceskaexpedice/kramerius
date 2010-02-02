package cz.i.kramerius.gwtviewers.client.panels;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

import cz.i.kramerius.gwtviewers.client.panels.fx.Rotate;
import cz.i.kramerius.gwtviewers.client.panels.utils.Helper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

/**
 * FX panel for moving pictures
 * @author pavels
 */
public class MoveEffectsPanel extends  Composite {
	
	private ImageRotatePool imageRotatePool;
	private ImageRotateCalculatedPositions imageRotateCalculatedPositions;
	private AbsolutePanel absolutePanel = new AbsolutePanel();
	
	private CoreConfiguration configuration;
	
	private MoveListener moveHandler;
	
	public MoveEffectsPanel( ImageMoveWrapper[] viewPortImages, 
							ImageMoveWrapper[] noVisibleImgs, 
							ImageMoveWrapper left, 
							ImageMoveWrapper right,   
							CoreConfiguration conf) {
		super();

		this.imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(viewPortImages, noVisibleImgs, left, right);
		this.imageRotatePool = new ImageRotatePool(viewPortImages, noVisibleImgs, left, right, 5 /* ! View port size !*/);
		
		this.configuration = conf;

		this.calulateNextPositions();
		this.storeCalculatedPositions();
		for (int i = 0; i < viewPortImages.length; i++) {
			ImageMoveWrapper img = viewPortImages[i];
			ImageMoveWrapper calculated = this.imageRotateCalculatedPositions.getViewPortCalulcatedPosition(i);
			img.setX(calculated.getX());
			img.setY(calculated.getY());
			this.absolutePanel.add(img.getWidget(), img.getX(), img.getY());
		}
		
		
		ImageMoveWrapper leftSideImage = this.imageRotatePool.getLeftSideImage();
		this.absolutePanel.add(leftSideImage.getWidget(), leftSideImage.getX(), leftSideImage.getY());

		ImageMoveWrapper rightSideImage = this.imageRotatePool.getRightSideImage();
		this.absolutePanel.add(rightSideImage.getWidget(), rightSideImage.getX(), rightSideImage.getY());

		for (int i = 0; i < noVisibleImgs.length; i++) {
			ImageMoveWrapper img = noVisibleImgs[i];
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
		if (this.moveHandler != null) {
			this.moveHandler.onMoveLeft(this.imageRotatePool);
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
			if (this.moveHandler != null) {
				this.moveHandler.onMoveRight(this.imageRotatePool);
			}
		
	}
	
	public ImageRotatePool getRotatePool() {
		return this.imageRotatePool;
	}

	public void calulateNextPositions() {
		Helper.computePositions(this.imageRotatePool, this.imageRotateCalculatedPositions,this.configuration);
	}


	public void storeCalculatedPositions() {
		Helper.storePositions(this.imageRotatePool, this.imageRotateCalculatedPositions, this.configuration);
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
	}



	public MoveListener getMoveHandler() {
		return moveHandler;
	}



	public void setMoveHandler(MoveListener moveHandler) {
		this.moveHandler = moveHandler;
	}
	
	
}
