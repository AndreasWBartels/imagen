module org.eclipse.imagen {
  exports org.eclipse.imagen.operator;
  exports org.eclipse.imagen.media.util;
  exports org.eclipse.imagen.util;
  exports org.eclipse.imagen.registry;
  exports org.eclipse.imagen.media.opimage;
  exports org.eclipse.imagen.media.iterator;
  exports org.eclipse.imagen.media.widget;
  exports org.eclipse.imagen.widget;
  exports org.eclipse.imagen.media.remote;
  exports org.eclipse.imagen.media.rmi;
  exports org.eclipse.imagen.media.test;
  exports org.eclipse.imagen.media.tilecodec;
  exports org.eclipse.imagen.remote;
  exports org.eclipse.imagen.iterator;
  exports org.eclipse.imagen.tilecodec;
  exports org.eclipse.imagen;

  requires java.desktop;
  requires java.rmi;
  requires org.eclipse.imagen.media.codec;

  uses org.eclipse.imagen.OperationRegistrySpi;
  uses org.eclipse.imagen.media.remote.JAIServerConfigurationSpi;

}