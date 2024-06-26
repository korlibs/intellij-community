// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vcs.log.paint

import com.intellij.ui.JBColor
import com.intellij.util.SmartList
import com.intellij.vcs.log.graph.EdgePrintElement
import com.intellij.vcs.log.graph.NodePrintElement
import com.intellij.vcs.log.graph.PrintElement
import com.intellij.vcs.log.graph.impl.print.elements.TerminalEdgePrintElement
import java.awt.*
import java.awt.geom.Ellipse2D
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

/**
 * @author erokhins
 */
internal open class SimpleGraphCellPainter(private val colorGenerator: ColorGenerator) : GraphCellPainter {
  protected open val rowHeight: Int get() = PaintParameters.ROW_HEIGHT

  private fun getDashLength(edgeLength: Double): FloatArray {
    // If the edge is vertical, then edgeLength is equal to rowHeight. Exactly one dash and one space fits on the edge,
    // so spaceLength + dashLength is also equal to rowHeight.
    // When the edge is not vertical, spaceLength is kept the same, but dashLength is chosen to be slightly greater
    // so that the whole number of dashes would fit on the edge.

    val rowHeight = rowHeight
    val dashCount = max(1, floor(edgeLength / rowHeight).toInt())
    val spaceLength = rowHeight / 2.0f - 2
    val dashLength = (edgeLength / dashCount - spaceLength).toFloat()
    return floatArrayOf(dashLength, spaceLength)
  }

  private val ordinaryStroke: BasicStroke
    get() = BasicStroke(PaintParameters.getLineThickness(rowHeight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)

  private val selectedStroke: BasicStroke
    get() = BasicStroke(PaintParameters.getSelectedLineThickness(rowHeight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)

  private fun getDashedStroke(dash: FloatArray): Stroke {
    return BasicStroke(PaintParameters.getLineThickness(rowHeight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0f, dash,
                       dash[0] / 2)
  }

  private fun getSelectedDashedStroke(dash: FloatArray): Stroke {
    return BasicStroke(PaintParameters.getSelectedLineThickness(rowHeight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0f, dash,
                       dash[0] / 2)
  }

  private fun paintUpLine(g2: Graphics2D, color: Color, from: Int, to: Int, hasArrow: Boolean, isUsual: Boolean, isSelected: Boolean,
                          isTerminal: Boolean) {
    // paint vertical lines normal size
    // paint non-vertical lines twice the size to make them dock with each other well
    val nodeWidth = PaintParameters.getNodeWidth(rowHeight)
    if (from == to) {
      val x = nodeWidth * from + nodeWidth / 2
      val y1 = rowHeight / 2 - 1
      val y2 = if (isTerminal) PaintParameters.getCircleRadius(rowHeight) / 2 + 1 else 0
      paintLine(g2, color, hasArrow, x, y1, x, y2, x, y2, isUsual, isSelected)
    }
    else {
      assert(!isTerminal)
      val x1 = nodeWidth * from + nodeWidth / 2
      val y1 = rowHeight / 2
      val x2 = nodeWidth * to + nodeWidth / 2
      val y2 = -rowHeight / 2
      paintLine(g2, color, hasArrow, x1, y1, x2, y2, (x1 + x2) / 2, (y1 + y2) / 2, isUsual, isSelected)
    }
  }

  private fun paintDownLine(g2: Graphics2D, color: Color, from: Int, to: Int, hasArrow: Boolean, isUsual: Boolean, isSelected: Boolean,
                            isTerminal: Boolean) {
    val nodeWidth = PaintParameters.getNodeWidth(rowHeight)
    if (from == to) {
      val y2 = rowHeight - (if (isTerminal) PaintParameters.getCircleRadius(rowHeight) / 2 + 1 else 0)
      val y1 = rowHeight / 2
      val x = nodeWidth * from + nodeWidth / 2
      paintLine(g2, color, hasArrow, x, y1, x, y2, x, y2, isUsual, isSelected)
    }
    else {
      assert(!isTerminal)
      val x1 = nodeWidth * from + nodeWidth / 2
      val y1 = rowHeight / 2
      val x2 = nodeWidth * to + nodeWidth / 2
      val y2 = rowHeight + rowHeight / 2
      paintLine(g2, color, hasArrow, x1, y1, x2, y2, (x1 + x2) / 2, (y1 + y2) / 2, isUsual, isSelected)
    }
  }

  private fun paintLine(g2: Graphics2D, color: Color, hasArrow: Boolean, x1: Int, y1: Int, x2: Int, y2: Int,
                        startArrowX: Int, startArrowY: Int, isUsual: Boolean, isSelected: Boolean) {
    g2.color = color
    g2.stroke = if (isUsual || hasArrow) {
      getUsualStroke(isSelected)
    }
    else {
      val edgeLength = if (x1 == x2) rowHeight.toDouble() else hypot((x1 - x2).toDouble(), (y1 - y2).toDouble())
      getDashedStroke(edgeLength, isSelected)
    }

    g2.drawLine(x1, y1, x2, y2)
    if (hasArrow) {
      val (endArrowX1, endArrowY1) = rotate(x1.toDouble(), y1.toDouble(), startArrowX.toDouble(), startArrowY.toDouble(),
                                            sqrt(ARROW_ANGLE_COS2), sqrt(1 - ARROW_ANGLE_COS2),
                                            ARROW_LENGTH * rowHeight)
      val (endArrowX2, endArrowY2) = rotate(x1.toDouble(), y1.toDouble(), startArrowX.toDouble(), startArrowY.toDouble(),
                                            sqrt(ARROW_ANGLE_COS2), -sqrt(1 - ARROW_ANGLE_COS2),
                                            ARROW_LENGTH * rowHeight)
      g2.drawLine(startArrowX, startArrowY, endArrowX1, endArrowY1)
      g2.drawLine(startArrowX, startArrowY, endArrowX2, endArrowY2)
    }
  }

  private fun paintCircle(g2: Graphics2D, position: Int, color: Color, select: Boolean) {
    val nodeWidth = PaintParameters.getNodeWidth(rowHeight)

    val x0 = nodeWidth * position + nodeWidth / 2
    val y0 = rowHeight / 2
    val r = if (select) PaintParameters.getSelectedCircleRadius(rowHeight) else PaintParameters.getCircleRadius(rowHeight)

    val circle = Ellipse2D.Double(x0 - r + 0.5, y0 - r + 0.5, (2 * r).toDouble(), (2 * r).toDouble())
    g2.color = color
    g2.fill(circle)
  }

  private fun getUsualStroke(select: Boolean): Stroke = if (select) selectedStroke else ordinaryStroke

  private fun getDashedStroke(edgeLength: Double, select: Boolean): Stroke {
    val length = getDashLength(edgeLength)
    return if (select) getSelectedDashedStroke(length) else getDashedStroke(length)
  }

  private fun getColor(printElement: PrintElement, isSelected: Boolean): Color {
    return if (isSelected) MARK_COLOR else colorGenerator.getColor(printElement.colorId)
  }

  override fun draw(g2: Graphics2D, printElements: Collection<PrintElement>) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val selected: MutableList<PrintElement> = SmartList()
    for (printElement in printElements) {
      if (printElement.isSelected) {
        selected.add(printElement) // to draw later
      }
      else {
        drawElement(g2, printElement, false)
      }
    }

    // draw selected elements
    for (printElement in selected) {
      drawElement(g2, printElement, true)
    }

    for (printElement in selected) {
      drawElement(g2, printElement, false)
    }
  }

  private fun drawElement(g2: Graphics2D, printElement: PrintElement, isSelected: Boolean) {
    if (printElement is EdgePrintElement) {
      printEdge(g2, getColor(printElement, isSelected), isSelected, printElement)
    }

    if (printElement is NodePrintElement) {
      paintCircle(g2, printElement.positionInCurrentRow, getColor(printElement, isSelected), isSelected)
    }
  }

  private fun printEdge(g2: Graphics2D, color: Color, isSelected: Boolean, edgePrintElement: EdgePrintElement) {
    val from = edgePrintElement.positionInCurrentRow
    val to = edgePrintElement.positionInOtherRow
    val isUsual = isUsual(edgePrintElement.lineStyle)
    val isTerminal = edgePrintElement is TerminalEdgePrintElement

    if (edgePrintElement.type == EdgePrintElement.Type.DOWN) {
      paintDownLine(g2, color, from, to, edgePrintElement.hasArrow(), isUsual, isSelected, isTerminal)
    }
    else {
      paintUpLine(g2, color, from, to, edgePrintElement.hasArrow(), isUsual, isSelected, isTerminal)
    }
  }

  override fun getElementUnderCursor(printElements: Collection<PrintElement>, x: Int, y: Int): PrintElement? {
    val nodeWidth = PaintParameters.getNodeWidth(rowHeight)
    val circleRadius = PaintParameters.getCircleRadius(rowHeight)
    for (printElement in printElements) {
      if (printElement is NodePrintElement) {
        if (PositionUtil.overNode(printElement.positionInCurrentRow, x, y, rowHeight, nodeWidth, circleRadius)) {
          return printElement
        }
      }
    }

    val lineThickness = PaintParameters.getLineThickness(rowHeight)
    for (printElement in printElements) {
      if (printElement is EdgePrintElement) {
        val position = printElement.positionInCurrentRow
        val positionInOtherRow = printElement.positionInOtherRow
        if (printElement.type == EdgePrintElement.Type.DOWN) {
          if (PositionUtil.overDownEdge(position, positionInOtherRow, x, y, rowHeight, nodeWidth, lineThickness)) {
            return printElement
          }
        }
        else {
          if (PositionUtil.overUpEdge(positionInOtherRow, position, x, y, rowHeight, nodeWidth, lineThickness)) {
            return printElement
          }
        }
      }
    }
    return null
  }

  companion object {
    private val MARK_COLOR: Color = JBColor.BLACK
    private const val ARROW_ANGLE_COS2 = 0.7
    private const val ARROW_LENGTH = 0.3

    private fun rotate(x: Double, y: Double, centerX: Double, centerY: Double, cos: Double, sin: Double,
                       arrowLength: Double): Pair<Int, Int> {
      val translateX = (x - centerX)
      val translateY = (y - centerY)

      val d = hypot(translateX, translateY)
      val scaleX = arrowLength * translateX / d
      val scaleY = arrowLength * translateY / d

      val rotateX = scaleX * cos - scaleY * sin
      val rotateY = scaleX * sin + scaleY * cos

      return Pair(Math.round(rotateX + centerX).toInt(), Math.round(rotateY + centerY).toInt())
    }

    private fun isUsual(lineStyle: EdgePrintElement.LineStyle): Boolean {
      return lineStyle == EdgePrintElement.LineStyle.SOLID
    }
  }
}
