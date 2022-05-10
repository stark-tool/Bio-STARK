

import numpy.random as rnd
import matplotlib.pyplot as plt
import numpy
from statistics import mean

TEMP_off = numpy.genfromtxt("testTemperature.csv", names=["temp_off"])
TEMP_off1 = numpy.genfromtxt("testTemperature1.csv", names=["temp_off1"])
TEMP_off2 = numpy.genfromtxt("testTemperature2.csv", names=["temp_off2"])

fix, ax = plt.subplots()
ax.plot(range(90,300),TEMP_off1['temp_off1'],label="l_o = -1")
ax.plot(range(90,300),TEMP_off['temp_off'],label="l_o = -1.5")
ax.plot(range(90,300),TEMP_off2['temp_off2'],label="l_o = -2")
legend = ax.legend()
plt.title("Variation of temperature wrt different offset intervals")
plt.savefig("temperature.png")
plt.show()

WRN_tau = numpy.genfromtxt("testWarning_tau.csv", names=["wrn_tau"])

plt.plot(range(90,210), WRN_tau['wrn_tau'])
plt.title("IDS warning lelvel for tau = 100")
plt.savefig("wrn_100.png")
plt.show()

WRN_tau2 = numpy.genfromtxt("testWarning_tau2.csv", names=["wrn_tau2"])

plt.plot(range(240,360), WRN_tau2['wrn_tau2'])
plt.title("IDS warning level for tau = 250")
plt.savefig("wrn_250.png")
plt.show()

WRN_tau3 = numpy.genfromtxt("testWarning_tau3.csv", names=["wrn_tau3"])

plt.plot(range(290,410), WRN_tau3['wrn_tau3'])
plt.title("IDS warning level for tau = 300")
plt.savefig("wrn_300.png")
plt.show()

STRESS_off = numpy.genfromtxt("testStress.csv", names=["stress_off"])
STRESS_off1 = numpy.genfromtxt("testStress1.csv", names=["stress_off1"])
STRESS_off2 = numpy.genfromtxt("testStress2.csv", names=["stress_off2"])

fix, ax = plt.subplots()
ax.plot(range(90,220),STRESS_off1['stress_off1'],label="l_o = -1")
ax.plot(range(90,220),STRESS_off['stress_off'],label="l_o = -1.5")
ax.plot(range(90,220),STRESS_off2['stress_off2'],label="l_o = -2")
legend = ax.legend()
plt.title("Variation of stress wrt different offset intervals")
plt.savefig("stress.png")
plt.show()

WRN_max = numpy.genfromtxt("testIntervalWarn.csv", names=["wrn_max"])
STRESS_max = numpy.genfromtxt("testIntervalSt.csv", names=["stress_max"])

plt.plot(range(0,50),WRN_max['wrn_max'])
plt.title("Evaluation of distances wrt warning over time")
plt.savefig("time_wrn.png")
plt.show()

plt.plot(range(0,50),STRESS_max['stress_max'])
plt.title("Evaluation of distances wrt stress over time")
plt.savefig("time_stress.png")
plt.show()


fix, ax = plt.subplots()
ax.plot(range(0,50),WRN_max['wrn_max'],label="warning")
ax.plot(range(0,50),STRESS_max['stress_max'],label="stress")
legend = ax.legend()
plt.title("Evaluation of distances over time")
plt.savefig("time.png")
plt.show()