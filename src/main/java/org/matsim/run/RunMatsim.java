/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.Modes4Accessibility;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author nagel
 *
 */
public class RunMatsim {

	private static double populationSample = 0.5;

	public static void main(String[] args) {
		
		Config config ;
		if ( args.length==0 || args[0]=="" ) {
			config = ConfigUtils.loadConfig( "config_horizon_2021_1_car.xml" ) ;
			config.controler().setLastIteration(1);
			config.qsim().setFlowCapFactor(0.07);
			config.qsim().setStorageCapFactor(0.1);
			config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		} else {
			config = ConfigUtils.loadConfig(args[0]) ;
		}
		
		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		boolean scalePopulation = true;
		reducePopulation(scenario, scalePopulation);
		Controler controler = new Controler( scenario ) ;

		boolean calculateAccessibility = true;
		if (calculateAccessibility){
			AccessibilityConfigGroup accConfig = ConfigUtils.addOrGetModule(config, AccessibilityConfigGroup.class ) ;
			accConfig.setComputingAccessibilityForMode(Modes4Accessibility.freespeed, true);
			accConfig.setComputingAccessibilityForMode(Modes4Accessibility.car, true);
		}
		controler.run();


	}

	private static void reducePopulation(Scenario scenario, boolean scalePopulation) {
		if (scalePopulation){
			List<Id<Person>> personIdList2 = new LinkedList<Id<Person>>();

			Iterator personIterator = scenario.getPopulation().getPersons().values().iterator();
			while (personIterator.hasNext()) {
				Person person = (Person) personIterator.next();
				personIdList2.add(person.getId());
           /*
            Id<Person> toAddId = (Id<Person>) randomDrawIterator.next();
            Person toAddPerson = population.getPersons(Map<Id<Person>, args> );
            drawedPopulation.addPerson(toRemoveId);
            */
			}
			List<Id<Person>> randomDraw = pickNRandom(personIdList2, personIdList2.size() * (1-populationSample));
			Iterator randomDrawIterator = randomDraw.iterator();
			while (randomDrawIterator.hasNext()) {
				Id<Person> toRemoveId = (Id<Person>) randomDrawIterator.next();
				log.println("Removing the person " + toRemoveId);
				scenario.getPopulation().removePerson(toRemoveId);
			}
		}
	}

	public static List<Id<Person>> pickNRandom (List < Id < Person >> lst, double n){
		List<Id<Person>> copy = new LinkedList<Id<Person>>(lst);
		Collections.shuffle(copy);
		return copy.subList(0, (int) n);
	}

}
