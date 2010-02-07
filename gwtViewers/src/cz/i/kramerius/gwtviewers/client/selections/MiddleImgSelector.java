package cz.i.kramerius.gwtviewers.client.selections;

import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;

public class MiddleImgSelector implements Selector {

	private MidleImgSelectDecorator decorator;
	private int center;
	
	private ImageRotatePool _shadowInstance;
	private int _shadowCenter = -1;
	private int _shadowDiv = -1;
	
	public MiddleImgSelector(int center) {
		super();
		this.center = center;
	}

	@Override
	public SelectionDecorator createSelectionDecorator() {
		this.decorator = new MidleImgSelectDecorator(this, this.center);
		return decorator;
	}

	@Override
	public ImageMoveWrapper getSelection(ImageRotatePool rotatePool) {
		int div = shadowCenter(rotatePool);
		return rotatePool.getViewPortImage(div);
	}

	private int shadowCenter(ImageRotatePool rotatePool) {
		if ((rotatePool != _shadowInstance) || (_shadowCenter == -1)){
			_shadowInstance = rotatePool;
			int viewPortSize = rotatePool.getViewPortSize();
			int div = viewPortSize/2;
			int mod =  viewPortSize % 2;
			if (mod == 1) div += mod;
			div -=1;
			this._shadowCenter = div;
			return div;
		} else {
			return _shadowCenter;
		}
	}

	public int selectionToSliderPosition(ImageRotatePool pool, int what) {
		int div = shadowDistance(pool);
		int currentLeft = what - div;
		return currentLeft;
	}
	
	public int shadowDistance(ImageRotatePool pool) {
		if ((pool != this._shadowInstance) || (_shadowDiv == -1)){
			this._shadowInstance = pool;
			int viewPortSize = pool.getViewPortSize();
			int div = viewPortSize/2;
			this._shadowDiv = div;
			return div;
		} else {
			return this._shadowDiv;
		}
	}
	
	public  int sliderPositionToSelection(ImageRotatePool pool, int what) {
		int div = shadowDistance(pool);
		int currentSel = what +div;
		return currentSel;
	}
	
	@Override
	public boolean isSelected(ImageMoveWrapper wrapper, ImageRotatePool pool) {
		return wrapper == getSelection(pool);
	}

	@Override
	public SelectionDecorator getSelectionDecorator() {
		if (this.decorator == null) {
			return createSelectionDecorator();
		} else {
			return this.decorator;
		}
	}

	public int moveToSelect(ImageMoveWrapper wrapper, ImageRotatePool pool) {
		if (this.isSelected(wrapper, pool)) {
			return 0;
		} else {
			ImageMoveWrapper selection = getSelection(pool);
			int selectedIndex = selection.getIndex();
			int wrapped = wrapper.getIndex();
			return  wrapped - selectedIndex;
		}
	}
}
