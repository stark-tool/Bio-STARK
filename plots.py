# -*- coding: utf-8 -*-
"""
Created on Mon May  9 13:45:11 2022

@author: valentinac
"""


import numpy.random as rnd
import matplotlib.pyplot as plt
import numpy
from statistics import mean

data_tau = numpy.genfromtxt("testDistance_tau.csv", names=["dist_tau"])

plt.plot(range(90,210), data_tau['dist_tau'])
plt.show()

data_tau2 = numpy.genfromtxt("testDistance_tau2.csv", names=["dist_tau2"])

plt.plot(range(240,360), data_tau2['dist_tau2'])
plt.show()

data_tau3 = numpy.genfromtxt("testDistance_tau3.csv", names=["dist_tau3"])

plt.plot(range(290,410), data_tau3['dist_tau3'])
plt.show()