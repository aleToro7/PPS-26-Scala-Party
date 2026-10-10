package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import com.unibo.scalaparty.core.utils.half

extension (self: AABB)

  /** Checks if this AABB intersects with another AABB. */
  def intersects(other: AABB): Boolean =
    val dx = math.abs(self.center.x - other.center.x)
    val dy = math.abs(self.center.y - other.center.y)
    dx <= self.width.half + other.width.half && dy <= self.height.half + other.height.half

  /** Checks if this AABB intersects with a Circle. */
  def intersects(circle: Circle): Boolean = circle.intersects(self)

  /** Checks if this AABB intersects with a Polygon. */
  def intersects(polygon: Polygon): Boolean = polygon.intersects(self)

  /** Calculates the minimum translation vector needed to separate two intersecting AABBs. */
  def penetratingVector(other: AABB): Option[Vector2D] =
    val delta = other.center - self.center
    val overlapX = (self.width.half + other.width.half) - math.abs(delta.x)
    val overlapY = (self.height.half + other.height.half) - math.abs(delta.y)
    Option.when(overlapX > 0 && overlapY > 0):
      if overlapX < overlapY then
        Vector2D(if delta.x < 0 then -overlapX else overlapX, 0.0)
      else
        Vector2D(0.0, if delta.y < 0 then -overlapY else overlapY)

  /** Calculates the minimum translation vector to separate this AABB from a Circle. */
  def penetratingVector(circle: Circle): Option[Vector2D] = circle.penetratingVector(self).map(-_)

  /** Calculates the minimum translation vector to separate this AABB from a Polygon. */
  def penetratingVector(polygon: Polygon): Option[Vector2D] = polygon.penetratingVector(self).map(-_)

extension (self: Circle)

  /** Checks if this Circle intersects with another Circle. */
  def intersects(other: Circle): Boolean =
    val distanceSquared = (self.center - other.center).moduleSquared
    val radiusSum = self.radius + other.radius
    distanceSquared <= radiusSum * radiusSum

  /** Checks if this Circle intersects with an AABB. */
  def intersects(aabb: AABB): Boolean =
    val delta = self.center - aabb.center
    val halfWidth = aabb.width.half
    val halfHeight = aabb.height.half
    val clampedX = Math.clamp(delta.x, -halfWidth, halfWidth)
    val clampedY = Math.clamp(delta.y, -halfHeight, halfHeight)
    val closestPoint = Point2D(aabb.center.x + clampedX, aabb.center.y + clampedY)
    (self.center - closestPoint).moduleSquared <= self.radius * self.radius

  /** Checks if this Circle intersects with a Polygon. */
  def intersects(polygon: Polygon): Boolean =
    polygon.intersects(self)

  /** Calculates the minimum translation vector needed to separate two intersecting circles. */
  def penetratingVector(other: Circle): Option[Vector2D] =
    val delta = other.center - self.center
    val distance = delta.module
    val overlap = self.radius + other.radius - distance
    Option.when(overlap > 0):
      val direction = if distance == 0.0 then Vector2D.unitX else delta.normalized
      direction * overlap

  /** Calculates the minimum translation vector to separate this Circle from an AABB. */
  def penetratingVector(aabb: AABB): Option[Vector2D] =
    val delta = aabb.center - self.center
    val halfWidth = aabb.width.half
    val halfHeight = aabb.height.half
    val clampedX = Math.clamp(self.center.x - aabb.center.x, -halfWidth, halfWidth)
    val clampedY = Math.clamp(self.center.y - aabb.center.y, -halfHeight, halfHeight)
    val closestPoint = Point2D(aabb.center.x + clampedX, aabb.center.y + clampedY)
    val distanceFromClosest = self.center - closestPoint
    val distanceSquared = distanceFromClosest.moduleSquared

    Option.when(distanceSquared < self.radius * self.radius):
      if distanceSquared == 0.0 then
        val overlapLeft = self.center.x - (aabb.center.x - halfWidth)
        val overlapRight = (aabb.center.x + halfWidth) - self.center.x
        val overlapBottom = self.center.y - (aabb.center.y - halfHeight)
        val overlapTop = (aabb.center.y + halfHeight) - self.center.y
        val minOverlapX = math.min(overlapLeft, overlapRight)
        val minOverlapY = math.min(overlapBottom, overlapTop)
        if minOverlapX < minOverlapY then
          val signX = if overlapLeft < overlapRight then 1.0 else -1.0
          Vector2D(signX * (minOverlapX + self.radius), 0.0)
        else
          val signY = if overlapBottom < overlapTop then 1.0 else -1.0
          Vector2D(0.0, signY * (minOverlapY + self.radius))
      else
        val distance = math.sqrt(distanceSquared)
        val overlap = self.radius - distance
        distanceFromClosest.normalized * overlap

  /** Calculates the minimum translation vector to separate this Circle from a Polygon. */
  def penetratingVector(polygon: Polygon): Option[Vector2D] =
    polygon.penetratingVector(self).map(-_)

extension (self: Polygon)

  /** Checks if this Polygon intersects with another Polygon using the Separating Axis Theorem (SAT). */
  def intersects(other: Polygon): Boolean =
    val axes = self.axes ++ other.axes
    !self.hasSeparatingAxis(axes)(other)

  /** Checks if this Polygon intersects with an AABB. */
  def intersects(aabb: AABB): Boolean =
    self.intersects(Polygon(aabb.vertices*))

  /** Checks if this Polygon intersects with a Circle. */
  def intersects(circle: Circle): Boolean =
    if self.hasSeparatingAxis(self.axes)(circle) then false
    else
      val closestVertex = self.closestVertexTo(circle.center)
      val vertexAxis = circle.center - closestVertex
      if vertexAxis.moduleSquared == 0.0 then true
      else
        val circleAxis = vertexAxis.normalized
        val pProj = self.projectOnto(circleAxis)
        val cProj = circle.projectOnto(circleAxis)
        pProj.overlaps(cProj)

  /** Calculates the minimum translation vector to separate this Polygon from another. */
  def penetratingVector(other: Polygon): Option[Vector2D] =
    computePolygonMtv(self, self.axes ++ other.axes, other, other.center)

  /** Calculates the minimum translation vector to separate this Polygon from an AABB. */
  def penetratingVector(aabb: AABB): Option[Vector2D] =
    self.penetratingVector(Polygon(aabb.vertices*))

  /** Calculates the minimum translation vector to separate this Polygon from a Circle. */
  def penetratingVector(circle: Circle): Option[Vector2D] =
    val closestVertex = self.closestVertexTo(circle.center)
    val vertexAxis = circle.center - closestVertex
    val candidateAxes =
      if vertexAxis.moduleSquared == 0.0 then self.axes
      else self.axes :+ vertexAxis.normalized
    computePolygonMtv(self, candidateAxes, circle, circle.center)

private def computePolygonMtv(
    source: Polygon,
    candidateAxes: Seq[Vector2D],
    target: Projectable,
    targetCenter: Point2D
): Option[Vector2D] =
  source.minOverlappingAxis(candidateAxes)(target).map: (axis, overlap) =>
    val direction = targetCenter - source.center
    val alignedAxis = if axis.dot(direction) < 0 then -axis else axis
    alignedAxis * overlap

extension (self: Shape)

  /** Determines whether this shape intersects with another shape. */
  def intersects(other: Shape): Boolean = (self, other) match
    case (a1: AABB, a2: AABB) => a1.intersects(a2)
    case (c1: Circle, c2: Circle) => c1.intersects(c2)
    case (p1: Polygon, p2: Polygon) => p1.intersects(p2)
    case (p: Polygon, a: AABB) => p.intersects(a)
    case (a: AABB, p: Polygon) => a.intersects(p)
    case (c: Circle, a: AABB) => c.intersects(a)
    case (a: AABB, c: Circle) => a.intersects(c)
    case (p: Polygon, c: Circle) => p.intersects(c)
    case (c: Circle, p: Polygon) => c.intersects(p)

  /** Calculates the minimum translation vector needed to separate two intersecting shapes. */
  def penetratingVector(other: Shape): Option[Vector2D] = (self, other) match
    case (a1: AABB, a2: AABB) => a1.penetratingVector(a2)
    case (c1: Circle, c2: Circle) => c1.penetratingVector(c2)
    case (p1: Polygon, p2: Polygon) => p1.penetratingVector(p2)
    case (p: Polygon, a: AABB) => p.penetratingVector(a)
    case (a: AABB, p: Polygon) => a.penetratingVector(p)
    case (c: Circle, a: AABB) => c.penetratingVector(a)
    case (a: AABB, c: Circle) => a.penetratingVector(c)
    case (p: Polygon, c: Circle) => p.penetratingVector(c)
    case (c: Circle, p: Polygon) => c.penetratingVector(p)
