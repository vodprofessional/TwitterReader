package com.vodprofessionals.socialexplorer.vaadin

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{Label, VerticalLayout}

/**
 *
 */
class AnalyticsView extends VerticalLayout with View {
  val label = new Label("Analytics here")
  addComponent(label)

  override def enter(event: ViewChangeEvent): Unit = {

  }
}
