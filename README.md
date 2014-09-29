# Fishery Simulation

In 2014 [Icosystem](http://www.icosystem.com), funded by a grant from [The Rockefeller Foundation](http://www.rockefellerfoundation.org/), 
created a software model of the Mindoro Philippines tuna fishery value chain from fish stock to consumer. This model was 
used to derive insights into, and improve our understanding of the long term effect of potential interventions governments 
and organizations could make in this fishery to achieve sustainability of the fish stock and improve the financial well 
being of traditional tuna fishers at the same time.

This repo contains the source code for that model as well as the web site that showcases how the model works and insights 
gained by looking at the model. This readme gives a quick overview of the high points of the model both conceptually and 
in the code base. It also touches on some basic architecture and what tools and libraries we used to develop the web site.

For instructions on setting up a development environment for this project, check new-developer-setup.txt

## Model Basics
The model is an agent based model that simulates the conditions in a fish supply chain over time. It considers 
(at a high level) everything from the amount of fish in the ocean, how many are caught by local fishermen all the way 
to tonnage of fish purchased by the end consumer. The Model is a "state updating tick based" model. That means agents keep
track of the state they care about (quantity of fish they have, price they sold fish at last time etc). Time is considered 
to have a smallest possible unit (aka the "tick"). The simulation proceeds by asking each agent what it wants to do in the 
next tick. The agent takes some action based on its internal state some logic and potentially the current state of other agents. 
After the simulation has figured out the "next state" of all the agents it takes a second pass through the agents and updates 
their current state to be the newly computed one. This proceeds until the the desired length of the simulation has been reached.
 
Briefly the simulation pseudo code looks something like this:

1. Initialization 
    - Agents are given initial state
2. Tick Loop
    1. Compute Next State (aka tick)
        - Loop over every agent and compute its next state based on agent logic and current states
    2. Update State
        - Loop over every agent and update it so it's current state is the corresponding state compute in the previous process
3. Until desired number of steps go back to step 2

This main loop can be see in code in the simulation.FisherySimulation object.

### Agents 
The model is organized around a fish supply chain. Each step in the chain is given its own agent which interacts 
with other agents (usual the "next" and "previous" agents in the supply chain). The following is a list of the major agents 
in order of the supply chain and their basic actions:

1. Fish
    * Get Fished 
    * Replenish themselves
2. Municipal Fisher
    * Fish
    * Sell Fish to Local Market
3. Middleman Market
    * Supplied by Municipal Fisher
    * Takes Demand from Middlemen
    * Sets price based on supply and demand
4. Middlemen
    * Buys from Middleman Market
    * Sells to Wholesale Market
5. Wholesale Market
    * Supplied by Middlemen
    * Takes Demand from Exporters
    * Sets price based on supply and demand
6. Exporter
    * Buys from Wholesale Market
    * Sells to Consumer Market
7. Consumer Market
    * Supplied by Exporter
    * Takes Demand from Consumer
    * Sets price based on supply and demand
8. Consumer
    * Has Demand based on population
    * Population increases

Each of these agents extends the simulation.agent.Agent abstract class and has it's own class in the simulation.agent package. 
It should be noted that every agent also has on it a current state object of the corresponding type. For example MunicipalFisher 
has a currentState field of type MunicipalFisherState. These are the states updated at the end of the tick loop.

#### Markets
Markets are essentially a coding artifact in order figure out what happens when supply meets demand. They determine when X supply
of fish meets Y demand of fish how much gets sold and at what price.

All the markets use the same code/logic which is located at simulation.agent.Market.

#### Commercial Fishers
While municipal fisher are the primary focus of the system Commercial fishers exist in the system. The are modeled simply
as something that puts additional pressure on the fish population. It is assumed that sell their fish external to the current
model.

The code for Commercial Fishers located at simulation.agent.CommercialFisher.

### Configuration

The model is highly configurable. In addition to their starting values all the agents (and several of the
interactions between agents) have tuning parameters that can be varied. For example if one believes that the population
of consumers is going to increase dramatically there is a "growthRate" setting in the Consumer agent that can be increased
to match the expected values.

model.ModelCofig is the primary configuration object and contains, as fields, the other configuration objects.

### Interventions

While the setup and modeling of the supply chain "as is" is obviously important, the goal of the simulation is to see
what effect various interventions would have on the system. To that end an "Intervention Agent" exists in the
simulation. Unlike the other agents it does not perform any actions by default. But rather based on how it is configured
modifies the actions other agents take. For example if one wanted to find out what would happen to the fishery if 
fishermen were trained by an external organization, rather then setting a value on the fisherman one would configure
the "interventionNumberOfPeopleTrained" setting on the Intervention Agent.

Since the project was sponsored by Rockefeller the intervention agent code is in simulation.agent.Rockefeller.

### Running the Simulation (Configuration and Results)

The simulation itself is implemented as a scala function. It takes in a ModelConfig and produces SimulationResults. The  
ModelConfig is a series of nested case classes which define some general things like length of simulation, but are
primarily made up of initial values for the agents state and their parameter values. These are largely pass through values
that are simply copied into the corresponding agent. i.e. ModelConfig has a ModelConfigFish on it which contains the values
"biomass" "carryingCapacity" and "growthRate". Biomass is the initial value of the biomass of the Fish agent and carryingCapacity
and growthRate are model parameters on the Fish agent.

The SimulationResults are essentially a collection of all states of the agent at every tick, as well as some additional 
computed data. For example the SimulationResult object has a fish object that has an array with one entry per tick containing
the FishState object that corresponds to that tick. Additionally the fish object has a sustainable yields array that
contains one entry per tick with a double that is the computed sustainable yield.

The model configuration is at model.ModelCofig
The Simulation itself is simulation.FisherySimulation
And the results are at simulation.SimulationResults

## The Web Site

The Web site is built on the [play framework](https://www.playframework.com/) written in [scala](http://www.scala-lang.org/). 
We primarily use the [eclipse IDE](https://www.eclipse.org/) for development with the [Play Plugin](http://scala-ide.org/). 
For information on Setting up eclipse with play [check here](https://www.playframework.com/documentation/2.2.x/IDE).

On the client side we are using [less](http://lesscss.org/) to generate our css. Play has built in support for it and you
can find the documentation [here](https://www.playframework.com/documentation/2.2.x/AssetsLess). For layout and some widgets
we're using [bootstrap](http://getbootstrap.com/). For dynamic elements we're using [angularjs](https://angularjs.org/). 

### Services

The site makes use of three services to run the model and retrieve results on the client side. These three services are all
initiated via javascript ajax calls, they all take some information about a simulation to run and return some json result.

A few quick things to note. Play has helpers for converting [scala objects to JSON and vice versa](https://www.playframework.com/documentation/2.2.x/ScalaJson) 
which we make heavy use of. In particular we created a ModelConfigUI class that is designed to be serialized to and from
JSON. It is located at controllers.ModelConfigUI and has a helper object that has JSON formatter which handles the
de/serialization. 

This object can be converted to a ModelConfig object via the "baseline" or "intervention" methods. The baseline method
produces a ModelConfig instance where the interventions have not been applied and interventions, obviously, includes them.
The reason both methods exist is that it is common to want to compare what happened with the interventions to what would
have happened without them. In other words being able to asses were the interventions meaningful.

Calls to these services are often initiated by [watching](https://docs.angularjs.org/api/ng/type/$rootScope.Scope) a angularjs 
model that corresponds to the model settings (i.e. whenever the user changes a model configuration it triggers a run/data request). 
In order to not overload the server these calls are debounced (restricted to one call every x seconds) via this [library](http://benalman.com/projects/jquery-throttle-debounce-plugin/).

The following format will be used to described the services

View: The play template that uses the service
Route: The play route associated with the service
Watch
    file: The file that defines the watch that calls the service
    code: The line of code that establishes the watch
Code
    file: The file that contains the client side call to the service
    code: The line of code that makes the call
Description:

A general service and what it's used for.

#### run
View: views.modelLanding.scala.html
Route: POST /model/run controllers.Application.runModel
Watch
    file: assetes.javascripts.controllers.js
    code: "$scope.$watch('config', ..."
Code
    file: assetes.javascripts.services.js
    code: "that.rerunSimulation = $.debounce(200, that.rerunSimulationRaw);"
Description:

This service is use by the "full mode page". It is passed the entire model configuration (serialized as JSON) and returns 
JSON results both for the baseline and intervention version of the model run. This data is used to populate the charts
on the page. It is debounced.

#### run2
View: views.disseminationMain.scala.html
Route: POST /model/run2 controllers.Application.runModel2
Watch
    file: views.disseminationMain.scala.html
    code: "$scope.$watch('model.inputs', ..."
Code
    file: assetes.javascripts.general-services.js
    code: "it.getJsonPlusDebounced = function(delay) {..."
Description:

This service initiates a model run based on the configuration (serialized as JSON) it gets but it returns only a model id
which can be used later to retrieve results later (see below). It is debounced. 

#### data
View: views.charts.lineChart.scala.html
Route: POST /results/:runid/:dataid controllers.Application.data(runid: String, dataid: String)
Watch
    file: views.charts.lineChart.scala.html
    code: "scope.$watch('@resultsIdPath', ..."
Code
    file: assetes.javascripts.util.js
    code: "REMOTE.requestResultTable = function(resultsId, tableId, successCb, errorCb) {..."
Description:

This service is uses in conjunction with run2. Basically run2 is used to initiate a simulation run for a config.
That call will return a result ID that is set on an angularjs model. That value is watched and when it changes the 
result ID is used to retrieve data. Since the calls to run2 are debounced the calls to data do not need to be.

## License

This project is licensed under [AGPL license](http://www.gnu.org/licenses/agpl-3.0.html)

Third Party and Open Source Software Licensing Information

Software includes third party software that is distributed and licensed under “Open Source” or other third party licenses, the 

terms and conditions of which may be applicable to and enforceable. In such case, the terms and conditions of the applicable 

Open Source or third party licenses governs the use of such software. Prior to using the software, it is licensee’s responsibility 

to review and agree to such licensing, copyright and warranty terms. Links to the applicable licenses are provided below: 

Libraries and Links

|                                                                          Third Party License and Link                                                                           |                                                                 Applicable Software Libraries                                                                 |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|                                                         [Apache V2, 01/2004](http://www.apache.org/licenses/LICENSE-2.0)                                                        |                                                                 Akka and Play Framework guava                                                                 |
|                                                               [GNU LGPL v3](http://www.gnu.org/licenses/lgpl.html)                                                              |                                                                           findbugs                                                                            |
|                                                             [BSD](http://www.opensource.org/licenses/bsd-license.php)                                                           |                                                                    Simple Build Tool (sbt)                                                                    |
|                                                             [MIT](http://www.opensource.org/licenses/mit-license.php)                                                           |                                                                  Bootstrap jQuery angularjs                                                                   |
|                             Copyright (c) 2010 "Cowboy" Ben Alman Dual licensed under the [MIT and GPL](http://benalman.com/about/license/licenses).                            |                          [jQuery throttle / debounce - v1.1 - 3/7/2010](http://benalman.com/projects/jquery-throttle-debounce-plugin/)                        |
| Copyright (c) 2008 Filament Group, Inc Dual licensed under the [MIT](filamentgroup.com/examples/mit-license.txt) and [GPL](filamentgroup.com/examples/gpl-license.txt) licenses | jQuery-Plugin - selectToUISlider - creates a UI slider component from a select element(s) by Scott Jehl, scott@filamentgroup.com http://www.filamentgroup.com |
|                                             [Scala License](http://www.scala-lang.org/node/146?_ga=1.122931019.1305266478.1405715689)                                           |                                                              Scala and the Scala IDE for Eclipse                                                              |
