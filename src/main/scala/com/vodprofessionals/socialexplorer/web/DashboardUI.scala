package com.vodprofessionals.socialexplorer.web

import com.vaadin.annotations.{Push, Title}
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.ui.{Label, VerticalLayout, UI}

/**
 *
 */
@Title("Social Explorer Dashboard")
@Push(PushMode.AUTOMATIC)
class DashboardUI extends UI {
  override def init(request: VaadinRequest): Unit = {
    val layout:VerticalLayout = new VerticalLayout
    setContent(layout)

    layout.addComponent(new Label("Hello World") )
  }
}
