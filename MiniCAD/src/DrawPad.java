import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Iterator;
import java.util.Vector;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;


/**
 * 画板
 * @author tgmerge
 */

@SuppressWarnings({ "unused", "serial" })
public class DrawPad extends JFrame implements MouseListener, MouseMotionListener{

	static public DrawPad drawPad;	// 单例的

	
	enum Stat		{ IDLE, SELECT, EDIT }			// 画板的状态
	
	private Shape.ShapeType brushShape = Shape.ShapeType.CIRCLE;
	private Color		    brushColor = Color.BLACK;
	private Stat		    brushStat  = Stat.IDLE;
	private int 			brushEdit  = 1;						// 操作中的手柄
	private String          brushText  = "Text to be drawn";    // 要画的文字
	
	private Vector<Shape> shapes 		= new Vector<Shape>();	// 已有形状的列表
	private Shape         shapeSelected = null;					// 现在选择中的形状
	private Image         background 	= null;					// 背景图片
	private Image         image         = null;                 // 显示内容
	private Graphics      graphics;								// Graphics
	
	// - - -
	
	/**
	 * 构造方法。创建窗体。
	 */
	public DrawPad() {
		drawPad = this;
		setTitle("MiniCAD");
		setSize(600, 400);
		setLocation(100, 100);
		setName("DrawPad");
		setVisible(true);
		setResizable(false);
		
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		graphics = image.getGraphics();
		graphics.setPaintMode();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		reDraw();
	}
	
	private void clearShapes() {
		shapes = new Vector<Shape>();
	}
	
	/**
	 * 改变画板的当前状态
	 */
	public void setStat(final Stat newStat) {
		System.out.println(newStat);
		brushStat = newStat;
	}
	public Stat getStat() {
		return brushStat;
	}
	
	
	/**
	 * 改变下一个形状类型
	 */
	public void setShape(final Shape.ShapeType newShape) {
		brushShape = newShape;
	}
	
	
	
	/**
	 * 改变下一个形状颜色
	 */
	public void setColor(final Color newColor) {
		brushColor = newColor;
		graphics.setColor(newColor);
	}
	
	/**
	 * 改变要写的文字
	 */
	public void setText(final String s) {
		brushText = s;
	}
	public String getText() {
		return brushText;
	}
	
	/**
	 * 取选择了的形状
	 */
	public Shape getShapeSelected() {
		return shapeSelected;
	}
	
	/**
	 * 改变背景图片
	 */
	public void setBackgroundImage(Image i) {
		shapeSelected = null;
		setStat(Stat.IDLE);
		clearShapes();
		background = i;
		reDraw();
	}
	public Image getImage() {
		shapeSelected = null;
		setStat(Stat.IDLE);
		reDraw();
		return image;
	}
	
	
	
	//- - -
	
	/**
	 * 按vector的顺序重绘画板，不考虑选择
	 */
	public void reDraw() {
		if(background != null) {
			graphics.drawImage(background, 0, 0, getWidth(), getHeight(), this);
		} else {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, getWidth(), getHeight());
		}
		Iterator<Shape> iter = shapes.iterator();
		while(iter.hasNext()) {
			iter.next().draw(graphics);
		}
		if(shapeSelected != null) {
			shapeSelected.drawWithResize(graphics);
		}
		this.getGraphics().drawImage(image, 0, 0, getWidth(), getHeight(), this);
	}

	
	/**
	 * 向画板添加一个形状并重绘画板
	 */
	private void addShape(Shape s) {
		shapes.addElement(s);
	    setStat(Stat.IDLE);
	    reDraw();
	}
	
	
	/**
	 * 选择一个形状并重绘画板
	 */
	private void selectShape(Shape s) {
		if(shapes.indexOf(s) == -1) {
			System.out.println("[selectShape]can't find shape");
			return;
		}
		shapeSelected = s;
		setStat(Stat.SELECT);
		reDraw();
	}
	
	
	/**
	 * 取消形状选择
	 */
	private void unselectShape() {
		shapeSelected = null;
		setStat(Stat.IDLE);
		reDraw();
	}
	
	
	/**
	 * 编辑形状
	 */
	private void editShape(Shape s, int point) {
		shapeSelected = s;
		setStat(Stat.EDIT);
		brushEdit = point;
		reDraw();
	}
	
	
	/**
	 * 退出编辑状态,回到选择状态
	 */
	private void uneditShape() {
		setStat(Stat.SELECT);
		reDraw();
	}
	
	
	/**
	 * 检查点击某点时最上方的那个形状。没有则返回null。
	 */
	private Shape topShapeByPos(int x, int y) {
		Iterator<Shape> iter = shapes.iterator();
		Shape result = null;
		Shape s = null;
		while(iter.hasNext()) {
			s = iter.next();
			if(s.isIn(x, y)) {
				result = s;
			}
		}
		return result;
	}

	
	/**
	 * 尝试按位置选择一个形状，失败返回null并回到idle，成功则进入选择状态并设置selected
	 */
	private Shape selectShapeByPos(int x, int y) {
		Shape s = topShapeByPos(x, y);
		if(s != null) {
			selectShape(s);
		} else {
			brushStat = Stat.IDLE;
		}
		return s;
	}
	
	
	
	// - - -
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		switch(getStat()) {
			case IDLE:
			case SELECT:
				/* Check if there's a shape under mouse.
				 *   if so, select it.
				 *   if not, unselect any shape.
				 */
				Shape s = topShapeByPos(e.getX(), e.getY());
				if(s != null) {
					selectShape(s);
				} else {
					unselectShape();
				}
				break;
			default:
				break;
		}
	}


	/**
	 * 按下按键的处理
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		switch(brushStat) {
			case SELECT:
				/* SEL时按下：若手柄在光标下则进入EDIT。
				 * 否则，更改已选形状
				 */
				if(shapeSelected.isOn2(e.getX(), e.getY())) {
					editShape(shapeSelected, 2);
				} else if(shapeSelected.isOn1(e.getX(), e.getY())) {
					editShape(shapeSelected, 1);
				} else {
					selectShapeByPos(e.getX(), e.getY());
				}
			default:
				break;
		}
	}

	/**
	 * 释放按键的处理
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		switch(getStat()) {
		case SELECT:
			/* SEL时释放：终止移动
			 */
			shapeSelected.endRepos();
			break;
		case EDIT:
			/* EDIT释放：进入SEL。
			 */
			uneditShape();
			break;
		default:
			break;
		}
		
	}


	/**
	 * 拖动鼠标的事件处理
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Shape s;
		switch(getStat()) {
			case EDIT:
				/* EDIT下拖动：更改大小
				 */
				shapeSelected.resize(brushEdit, e.getX(), e.getY());
				reDraw();
				break;
			case IDLE:
				/* IDLE下拖动：创建新形状，随即进入EDIT更改大小
				 * => EDIT(2)
				 */
				s = Shape.newShape(e.getX(), e.getY(), brushShape, brushColor);
				addShape(s);
				selectShape(s);
				editShape(s, 2);
				break;
			case SELECT:
				/* SEL下拖动：若在已经选择了的形状上则更改位置，否则取消选择进入IDLE
				 */
				if(shapeSelected.isIn(e.getX(), e.getY())) {
					shapeSelected.repos(e.getX(), e.getY());
					reDraw();
				} else {
					unselectShape();
				}
				break;
			default:
				break;
		}
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO 自动生成的方法存根	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自动生成的方法存根
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自动生成的方法存根
	}

}
