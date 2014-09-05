package com.vodprofessionals.socialexplorer.vaadin

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{VerticalLayout, Label}

/**
 *
 */
class ErrorView extends VerticalLayout with View {
  val label = new Label("Oops. The view you tried to navigate to doesn't exist.")
  addComponent(label)


  override def enter(p1: ViewChangeEvent): Unit = {

  }
}
