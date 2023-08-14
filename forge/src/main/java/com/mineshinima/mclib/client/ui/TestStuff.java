package com.mineshinima.mclib.client.ui;

import com.mineshinima.mclib.client.ui.space.Orientation;

public class TestStuff {

    public static UIElement createTestContainerColumn(float width) {
        return new UIElement()
                .width(width)
                .height(1F)
                .paddingLeft(16)
                .paddingRight(16)
                .backgroundColor(0,0,0,0);
    }

    public static UIElement createTestRow(UIElement... elements) {
        UIElement row = new UIElement()
                .width(1F)
                .height(128)
                .marginTop(10)
                .backgroundColor(0,0,0,0);

        float w = 1F / elements.length;
        for (UIElement element : elements) {
            element.height(1F);

            UIElement colContainer = TestStuff.createTestContainerColumn(w);
            colContainer.addChildren(element);
            row.addChildren(colContainer);
        }

        return row;
    }

    public static UIElement bigChungusTest() {
        UIElement wrapper = new UIViewport()
                .width(1F)
                .height(1F)
                .paddingLeft(84).paddingTop(10).paddingRight(50);

        UIElement test1 = new UIElement()
                .width(0.35F)
                .height(0.15F)
                .marginBottom(100)
                .marginTop(50)
                .marginLeft(0.1F)
                .borderColor(0,0,0,1)
                .borderWidth(5);

        UIElement test11 = new UIElement()
                .width(0.5F)
                .height(0.5F)
                .marginRight(50);
        test1.addChildren(test11);

        UIElement test111 = new UIElement()
                .width(0.5F)
                .height(0.5F)
                .marginRight(50);
        test1.addChildren(test111);

        UIElement test12 = new UIElement()
                .widthAuto()
                .height(0.5F);
        test1.addChildren(test12);

        UIElement test13 = new UIElement()
                .width(50)
                .height(50)
                .marginRight(50)
                .marginLeft(50);
        test12.addChildren(test13);

        UIElement test2 = new UIElement()
                .width(200)
                .height(0.15F)
                .marginLeft(20)
                .marginTop(20)
                .marginBottom(50);

        UIElement test3 = new UIElement()
                .width(300)
                .height(0.35F)
                .marginLeft(50)
                .marginTop(40);

        UIElement test4 = new UIElement()
                .width(250)
                .height(0.05F)
                .marginLeft(50)
                .marginTop(40);

        UIElement test5 = new UIElement()
                .width(300)
                .height(0.1F)
                .marginLeft(50)
                .marginTop(40);

        UIElement test6 = new UIElement()
                .width(200)
                .height(150)
                .marginLeft(50)
                .marginTop(40);

        UIElement test7 = new UIScrollElement()
                .scrollDirection(null)
                .overlayScrollbar(false)
                .width(0.5F)
                .height(150)
                .paddingBottom(50)
                .paddingTop(150)
                .paddingRight(50)
                .paddingLeft(50)
                .borderWidth(12)
                .borderColor(0,0,0,1)
                .wrap(false);

        UIElement testScroll2 = new UIScrollElement()
                .scrollDirection(Orientation.VERTICAL)
                .overlayScrollbar(true)
                .width(250)
                .height(150)
                .paddingBottom(50)
                .paddingTop(50)
                .paddingLeft(50)
                .borderWidth(4)
                .borderColor(0,0,0,1)
                .wrap(true);

        UIElement test8 = new UIElement()
                .width(200)
                .height(50);

        UIElement test9 = new UIElement()
                .widthAuto()
                .height(125)
                .marginLeft(50)
                .marginRight(50);

        UIElement test91 = new UIElement()
                .width(225)
                .height(25)
                .marginLeft(25)
                .marginTop(0.5F);
        test9.addChildren(test91);

        UIElement test10 = new UIElement()
                .width(550)
                .height(150)
                .marginTop(200);

        UIElement test50 = new UIElement()
                .width(400)
                .height(300)
                .paddingBottom(20)
                .paddingTop(20);
        test50.addChildren(test9);

        testScroll2.addChildren(new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50));

        test7.addChildren(new UIElement().width(50).height(50),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(150),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                testScroll2,
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50),
                new UIElement().width(50).height(50));

        wrapper.addChildren(test1, test2, test3, test4, test5, test6, test7, test8);

        Area a1 = new Area(10, 10, 10, 10);

        Area b1 = new Area(10,10,10,10);
        Area b2 = new Area(20,10,10,10);
        Area b3 = new Area(15,15,10,10);
        Area b4 = new Area(5,5,25,25);

        System.out.println(a1.intersect(b1));
        System.out.println(a1.intersect(b2));
        System.out.println(a1.intersect(b3));
        System.out.println(a1.intersect(b4));
        /* test commutativity */
        System.out.println(b1.intersect(a1));
        System.out.println(b2.intersect(a1));
        System.out.println(b3.intersect(a1));
        System.out.println(b4.intersect(a1));

        return wrapper;
    }
}
