/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.batch.phases;

import com.google.common.collect.Lists;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.BatchExtensionDictionnary;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.SonarIndex;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.batch.DecoratorsSelector;
import org.sonar.batch.DefaultDecoratorContext;
import org.sonar.batch.events.EventBus;

import java.util.Collection;
import java.util.List;

public class DecoratorsExecutor implements BatchComponent {

  private DecoratorsSelector decoratorsSelector;
  private SonarIndex index;
  private EventBus eventBus;
  private Project project;

  public DecoratorsExecutor(BatchExtensionDictionnary batchExtDictionnary,
      Project project, SonarIndex index, EventBus eventBus) {
    this.decoratorsSelector = new DecoratorsSelector(batchExtDictionnary);
    this.index = index;
    this.eventBus = eventBus;
    this.project = project;
  }

  public void execute() {
    Collection<Decorator> decorators = decoratorsSelector.select(project);
    eventBus.fireEvent(new DecoratorsPhaseEvent(Lists.newArrayList(decorators), true));
    decorateResource(project, decorators, true);
    eventBus.fireEvent(new DecoratorsPhaseEvent(Lists.newArrayList(decorators), false));
  }

  DecoratorContext decorateResource(Resource resource, Collection<Decorator> decorators, boolean executeDecorators) {
    List<DecoratorContext> childrenContexts = Lists.newArrayList();
    for (Resource child : index.getChildren(resource)) {
      boolean isModule = (child instanceof Project);
      DefaultDecoratorContext childContext = (DefaultDecoratorContext) decorateResource(child, decorators, !isModule);
      childrenContexts.add(childContext.setReadOnly(true));
    }

    DefaultDecoratorContext context = new DefaultDecoratorContext(resource, index, childrenContexts);
    if (executeDecorators) {
      for (Decorator decorator : decorators) {
        executeDecorator(decorator, context, resource);
      }
    }
    return context;
  }

  void executeDecorator(Decorator decorator, DefaultDecoratorContext context, Resource resource) {
    try {
      eventBus.fireEvent(new DecoratorExecutionEvent(decorator, true));
      decorator.decorate(resource, context);
      eventBus.fireEvent(new DecoratorExecutionEvent(decorator, false));

    } catch (Exception e) {
      // SONAR-2278 the resource should not be lost in exception stacktrace.
      throw new SonarException("Fail to decorate '" + resource + "'", e);
    }
  }

}
