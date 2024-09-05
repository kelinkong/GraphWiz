# graphDrawing 文件夹用于绘制图片
- 根据邻接矩阵绘制无向图
- 绘制坐标图
- 绘制性能对比图

## 绘图测试：
```java
    import org.junit.Test;
    import javax.swing.*;
    import java.awt.*;
    
    /**
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
```

但是java相关绘图环境不太行。建议python或matlab!