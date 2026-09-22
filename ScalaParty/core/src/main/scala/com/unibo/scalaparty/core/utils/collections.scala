package com.unibo.scalaparty.core.utils

import scala.reflect.ClassTag

extension (self: Iterable[Any])
  /** Collects and returns the first element matching the class [[C]]
   * @tparam C the class to match
   * @return the first element of type C, or None if no such element exists
   */
  def collectFirstOfClass[C: ClassTag]: Option[C] = self.collectFirst { case it: C => it }
