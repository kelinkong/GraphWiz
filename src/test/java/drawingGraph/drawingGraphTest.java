package drawingGraph;

import org.junit.Test;
import javax.swing.*;
import java.awt.*;


/**
 * @author wendao
 * @since 2024-09-02
 * 测试 java 画图的效果
 **/
public class drawingGraphTest extends JComponent{
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawLine(50,50,200,200);//图的顶点（x1,y1）(x2,y2)
    }

    @Test
    public void DrawingTest() throws InterruptedException {
        JFrame jFrame =new JFrame("Line Drawing Example");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //窗口大小
        jFrame.setSize(300,300);
        jFrame.add(new drawingGraphTest());
        jFrame.setVisible(true);
        //弹窗消失时间。@Test中需要，不然不显示
        Thread.sleep(10000);
    }

}
