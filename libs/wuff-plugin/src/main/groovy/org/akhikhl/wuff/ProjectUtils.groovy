/*
 * wuff
 *
 * Copyright 2014  Andrey Hihlovskiy.
 *
 * See the file "LICENSE" for copying and usage permission.
 */
package org.akhikhl.wuff

import java.nio.file.Paths

import groovy.transform.CompileStatic
import groovy.util.XmlParser

import org.apache.commons.codec.digest.DigestUtils

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.xml.sax.InputSource

/**
 * Project-centric utilities
 *
 * @author akhikhl
 */
final class ProjectUtils {

  private static final Logger log = LoggerFactory.getLogger(ProjectUtils)

  /**
   * Collects all ancestors + the given project.
   *
   * @param project project being analyzed, not modified.
   * @return list of projects, first element is root, last element is the given project.
   */
  static List<Project> collectWithAllAncestors(Project project) {
    List<Project> projects = []
    Project p = project
    while(p != null) {
      projects.add(0, p)
      p = p.parent
    }
    return projects
  }

  static Configuration findFileInProducts(Project project, File file) {
    project.configurations.find { config ->
      config.name.startsWith('product_') && config.find { it == file }
    }
  }

  /**
   * Finds a first project satisfying the given condition in the given ancestor chain.
   *
   * @param project project being analyzed, not modified.
   * @param condition closure which is repeatedly called against every project in the ancestor chain.
   *   If closure returns "truthy" value, the condition is satisfied and iteration breaks.
   * @return first project satisfying the given condition.
   */
  static Project findUpAncestorChain(Project project, Closure condition) {
    Project p = project
    while(p != null && !condition(p))
      p = p.parent
    return p
  }

  static stringToFile(String str, File file) {
    String fileMd5
    if(file.exists())
      file.withInputStream {
        fileMd5 = DigestUtils.md5Hex(it)
      }
    if(fileMd5 == DigestUtils.md5Hex(str))
      log.debug 'md5 of {} did not change - will not be overwritten', file
    else {
      log.debug 'md5 of {} changed - will be overwritten', file
      file.parentFile.mkdirs()
      file.text = str
    }
  }
}