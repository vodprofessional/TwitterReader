package com.vodprofessionals.socialexplorer.vaadin

import _root_.java.util.Locale

import com.vaadin.annotations.{Push, Theme}
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.{ThemeResource, Page, VaadinRequest}
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.MenuBar.Command
import com.vaadin.ui._
import com.vaadin.navigator.{Navigator, View}
import com.vodprofessionals.socialexplorer.vaadin.views.{SettingsView, ReportsView, DashboardView}
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
    "/reports"      -> classOf[ReportsView]
  )
  val nav = new Navigator(this, content)



  override def init(request: VaadinRequest): Unit = {
    setLocale(Locale.US)
    setContent(root)
    root.addStyleName("root")
    root.setSizeFull()

    buildMainView

  }

  private def buildMainView = {
    // Set up view navigation
    //nav.setErrorView(classOf[ErrorView])
    for ((uriFragment, viewClass) <- routes) yield nav.addView(uriFragment, viewClass)
    nav.addView("", new View() {
      override def enter(p1: ViewChangeEvent): Unit = {
        getUI.getNavigator.navigateTo("/dashboard")
      }
    })

    // Build layout
    root.addComponent(new HorizontalLayout {
      setSizeFull(); addStyleName("main-view")

      // Sidebar
      addComponent(
        new VerticalLayout {
          addStyleName("sidebar"); setWidth(null); setHeight("100%")

          // Sidebar :: Branding
          addComponent(new CssLayout {
            addStyleName("branding")
            val logo = new Label("<span>SocialExplorer</span> Dashboard", ContentMode.HTML)
            logo.setSizeUndefined()
            addComponent(logo)
          })

          // Sidebar :: Main menu controls
          {
            menu.addStyleName("menu"); menu.setHeight("100%")
            addComponent(menu); setExpandRatio(menu, 1)

            def generateMenuItems(items: List[String]): Map[String, Button] = items match {
              case uriFragment :: xs =>
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

                generateMenuItems(xs) + (uriFragment -> b)
              case Nil =>
                Map.empty[String, Button]
            }
            val uriFragmentsToButtons = generateMenuItems(routes.keySet.toList)

            // Build mapping of url calls to views
            val f = Page.getCurrent.getUriFragment match {
              case s: String if s.startsWith("!") => s.substring(1)
              case ""  => "/dashboard"
              case "/" => "/dashboard"
              case _   => "/dashboard"
            }
            uriFragmentsToButtons.get(f) match {
              case Some(b) =>
                b.addStyleName("selected")
                nav.navigateTo(f)
            }

          }

          // Sidebar :: User menu
          addComponent(new VerticalLayout() {
            setSizeUndefined(); addStyleName("user")
            val profilePic = new Image(null, new ThemeResource("img/profile-pic.png"))
            profilePic.setWidth("34px")
            addComponent(profilePic)
            val userName = new Label("User Name")
            userName.setSizeUndefined()
            addComponent(userName)

            val settings = new NativeButton("Settings")
            settings.addStyleName("icon-cog")
            settings.setDescription("Settings")
            addComponent(settings)

            nav.addView("/settings", classOf[SettingsView])

            settings.addClickListener(new Button.ClickListener {
              override def buttonClick(event: Button.ClickEvent) {
                nav.navigateTo("/settings")
              }
            })

            val exit = new NativeButton("Exit")
            exit.addStyleName("icon-cancel")
            exit.setDescription("Sign Out")
            addComponent(exit)

            exit.addClickListener(new Button.ClickListener {
              override def buttonClick(event: Button.ClickEvent) {
                // TODO Implement logout
                Notification.show("Not implemented in this version")
              }
            })
          })
      })

      // Content
      addComponent(content)
      content.setSizeFull()
      content.addStyleName("view-content")
      setExpandRatio(content, 1)
    })

  }


}
