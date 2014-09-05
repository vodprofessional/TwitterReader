package com.vodprofessionals.socialexplorer.vaadin

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{VerticalLayout, Label}

/**
 *
 */
class DashboardView extends VerticalLayout with View {
  val label = new Label("Dashboard")
  addComponent(label)


  override def enter(event: ViewChangeEvent): Unit = {

  }

}
