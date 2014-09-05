package com.vodprofessionals.socialexplorer.vaadin

import com.vaadin.server.{UIClassSelectionEvent, UIProvider}
import com.vaadin.ui.UI

/**
 *
 */
class DashboardUIProvider extends UIProvider {
  override def getUIClass(event: UIClassSelectionEvent): Class[_ <: UI] = {
    // We only have one UI now, later might need a mobile UI or somethign
    classOf[DashboardUI]
  }
}
