module core.main {
    requires static lombok;
    requires spring.context;
    requires org.reflections;


    exports com.jzargo.core.messages.command;
    exports com.jzargo.core.messages.event;
    exports com.jzargo.core.registry;
    exports com.jzargo.core.mapper;
}