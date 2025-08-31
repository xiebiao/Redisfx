module com.xiebiao.tools.redisfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires com.google.common;
    requires redis.clients.jedis;
    requires java.desktop;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;
    requires java.management;
    requires ch.qos.logback.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.materialdesign2;


    opens com.xiebiao.tools.redisfx to javafx.fxml;
    exports com.xiebiao.tools.redisfx;
    exports com.xiebiao.tools.redisfx.model;
    exports com.xiebiao.tools.redisfx.view;
    exports com.xiebiao.tools.redisfx.service.eventbus ;
    exports com.xiebiao.tools.redisfx.controller;
    opens com.xiebiao.tools.redisfx.controller to javafx.fxml;
}