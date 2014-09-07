package com.vodprofessionals.socialexplorer.vaadin.container

import _root_.java.sql.Connection
import _root_.java.util

import com.vaadin.addon.sqlcontainer.query.generator.filter.FilterToWhereTranslator
import com.vaadin.data.Container.Filter
import com.vaadin.data.util.sqlcontainer.RowItem
import com.vaadin.data.util.sqlcontainer.query.generator.StatementHelper
import com.vaadin.data.util.sqlcontainer.query.{OrderBy, FreeformStatementDelegate}
import com.vaadin.data.util.sqlcontainer.SQLUtil
import scala.collection.JavaConverters._


/**
 *
 */
class ServicesFreeformStatementDelegate extends FreeformStatementDelegate {
  protected var filters: List[Filter] = null
  protected var orderBys: List[OrderBy] = null


  override def getQueryStatement(offset: Int, limit: Int): StatementHelper = {
    val sh = new StatementHelper
    val query = new StringBuffer("SELECT * FROM services")
    if (filters != null)
      query.append(FilterToWhereTranslator.getWhereStringForFilters(filters.asJava, sh))
    query.append(getOrderByString)
    if (offset != 0 || limit != 0) {
      query.append(" LIMIT ").append(limit)
      query.append(" OFFSET ").append(offset)
    }
    sh.setQueryString(query.toString())
    sh
  }

  private def getOrderByString = {
    val  orderBuffer = new StringBuffer("")
    if (orderBys != null && !orderBys.isEmpty) {
      orderBuffer.append(" ORDER BY ")
      val lastOrderBy = orderBys.last
      for(orderBy <- orderBys) yield {
        orderBuffer.append(SQLUtil.escapeSQL(orderBy.getColumn()));
        if (orderBy.isAscending()) {
          orderBuffer.append(" ASC");
        } else {
          orderBuffer.append(" DESC");
        }
        if (orderBy != lastOrderBy) {
          orderBuffer.append(", ");
        }
      }
    }
    orderBuffer.toString()
  }

  override def getCountStatement: StatementHelper = ???

  override def getContainsRowQueryStatement(p1: AnyRef*): StatementHelper = ???

  override def storeRow(p1: Connection, p2: RowItem): Int = ???

  override def setOrderBy(p1: util.List[OrderBy]): Unit = ???

  override def getQueryString(p1: Int, p2: Int): String = ???

  override def getCountQuery: String = ???

  override def removeRow(p1: Connection, p2: RowItem): Boolean = ???

  override def setFilters(p1: util.List[Filter]): Unit = ???

  override def getContainsRowQueryString(p1: AnyRef*): String = ???
}
