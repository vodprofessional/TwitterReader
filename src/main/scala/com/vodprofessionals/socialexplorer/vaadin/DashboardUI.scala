package com.vodprofessionals.socialexplorer.vaadin

import _root_.java.util.Locale

import com.vaadin.annotations.{Push, Theme}
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui._
import com.vaadin.navigator.{Navigator, View}
import scala.collection.JavaConversions._

/**
 *
 */
@Push(PushMode.AUTOMATIC)
@Theme("dashboard")
@SerialVersionUID(1L)
class DashboardUI extends UI {
  val root = new CssLayout
  val menu = new CssLayout
  val content = new CssLayout
  val routes = Map[String, Class[_ <: View]](
    "/dashboard"    -> classOf[DashboardView],
    "/analytics"    -> classOf[AnalyticsView]
  )



  override def init(request: VaadinRequest): Unit = {
    val nav = new Navigator(this, content)

    setLocale(Locale.US)
    setContent(root)
    root.addStyleName("root")
    root.setSizeFull

    buildMainView(nav)

    nav.addView("", new View() {
      override def enter(p1: ViewChangeEvent): Unit = {
        getUI.getNavigator.navigateTo("/dashboard")
      }
    })
  }

  private def buildMainView(nav: Navigator): Unit = {
    //nav.setErrorView(classOf[ErrorView])
    for ((uriFragment, viewClass) <- routes) yield nav.addView(uriFragment, viewClass)

    root.addComponent(new HorizontalLayout {
      setSizeFull
      addStyleName("main-view")
      addComponent(new VerticalLayout {
        // Sidebar
        {
          addStyleName("sidebar")
          setWidth(null)
          setHeight("100%")

          // Branding
          addComponent(new CssLayout {
            addStyleName("branding")
            val logo = new Label("<span>SocialExplorer</span> Dashboard", ContentMode.HTML)
            logo.setSizeUndefined
            addComponent(logo)
            // addComponent(new Image(null, new ThemeResource("img/branding.png")))
          })

          // Main menu
          addComponent(menu)
          setExpandRatio(menu, 1)
        }
      })

      // Content
      addComponent(content)
      content.setSizeFull
      content.addStyleName("view-content")
      setExpandRatio(content, 1)
    })

    for (uriFragment <- routes.keySet) yield {
      val b = new NativeButton(uriFragment.substring(1, 2).toUpperCase + uriFragment.substring(2).replace("-", " "))
      b.addStyleName("icon-" + uriFragment.substring(1))
      b.addClickListener(new Button.ClickListener() {
        override def buttonClick(event: ClickEvent): Unit = {
          for(c <- menu.iterator().toList) yield c.removeStyleName("selected")
          event.getButton.addStyleName("selected")
          if (!(nav.getState == uriFragment))
            nav.navigateTo(uriFragment)
        }
      })

      menu.addComponent(b)
    }
    menu.addStyleName("menu")
    menu.setHeight("100%")
  }

}
