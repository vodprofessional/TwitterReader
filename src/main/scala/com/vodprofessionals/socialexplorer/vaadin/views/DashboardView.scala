package com.vodprofessionals.socialexplorer.vaadin.views

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui._

/**
 *
 */
class DashboardView extends VerticalLayout with View {
  UI.getCurrent().getPage().setTitle("Social Explorer Dashboard")
  setSizeFull(); addStyleName("dashboard-view")

  val top = new HorizontalLayout
  top.setWidth("100%"); top.setSpacing(true); top.addStyleName("toolbar")
  addComponent(top)

  val title = new Label("My Dashboard")
  title.setSizeUndefined()
  title.addStyleName("h1")
  top.addComponent(title)
  top.setComponentAlignment(title, Alignment.MIDDLE_LEFT)
  top.setExpandRatio(title, 1)


  override def enter(event: ViewChangeEvent): Unit = {

  }

}
