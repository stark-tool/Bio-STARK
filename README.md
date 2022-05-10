# JSPEAR

JSPEAR is a simple JAVA tool that is part of our framework for the specification and analysis properties of distances between the behaviours of systems operating in the presence of uncertainties.
This framework consists in:

  * A model for systems behaviour, called *evolution sequence*, that is defined as a sequence of probability measures over a set of application relevant data (called *data state*). This sequence represents the combined activity of the programs and environment that constitute the considered system.
  * A temporal logic, called *Robustness Temporal Logic* (*RobTL*), for the specification of the desired properties.
  * A *model checking algorithm*, based on stastical inference, for the verification of RobTL specifications.

Hence, JSPEAR includes:

 * A randomised procedure that, based on simulation, permits the estimation of the evolution sequence of a system s, assuming an initial data state d_s. Starting from d_s we sample N sequences of data states d_(0,j),...,d_(k,j), for j=1,...,N. All the data states collected at time i are used to estimate the distribution ES_(s,i), i.e., the i-th distribution in the evolution sequence of s.  
 * A procedure that given an evolution sequence permits to sample the effects of a perturbation over it. The same approach used to obtain an estimation of the evolution sequence associated with a given initial data state d_s can be used to obtain its perturbation. The only difference is that while for evolution sequences the data state obtained at step i+1 only depends on the data state at step $i$, here the effect of a perturbation p can be also applied. To guarantee statistical relevance of the collected data, for each sampled data state in the evolution sequence we use and additional number M of samplings to estimate the effects of p over it.
 * A mechanism to estimate the Wasserstein distance between two probability distributions over data states.
 * A function that permits evaluating a distance expression between an evolution sequence and its perturbed variant. A distance expression is evaluated, following a syntax driven procedure, by applying the estimation of the Wasserstein distance considered in the previous step at the involved time steps. 
 * A procedure that checks if a given evolution sequence satisfies a given formula.

The [lib](./lib) folder contains all the [JAVA classes](./lib/src/main/java/it/unicam/quasylab/jspear/) that are necessary to implement all the procedures, mechanisms, and functions described above.

In the [examples/engine](./examples/engine) folder you can find the script [Main.java](./examples/engine/src/main/java/it/unicam/quasylab/jspear/examples/engine/Main.java) containing various tests showcasing the various features of JSPEAR over the case study of a refrigerated engine system that is subject to cyber-physical attacks aimed at inflicting overstress of equipment. 

We have used the Python script [plots.py](./plots.py) to draw plots from the CSV files obtained from the JAVA script.

## Download 

To download JSPEAR you have just to clone GitHub project:

```
git clone https://github.com/quasylab/jspear.git
```

Run this command in the folder where you want to download the tool.
