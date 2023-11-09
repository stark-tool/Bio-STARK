

import numpy.random as rnd
import matplotlib.pyplot as plt
import numpy
from statistics import mean

TEMP_off = numpy.genfromtxt("testTemperature.csv", names=["temp_off"])
TEMP_off1 = numpy.genfromtxt("testTemperature_l1.csv", names=["temp_off1"])
TEMP_off2 = numpy.genfromtxt("testTemperature_l2.csv", names=["temp_off2"])

fix, ax = plt.subplots()
ax.plot(range(90,300),TEMP_off1['temp_off1'],label="l_o = -1")
ax.plot(range(90,300),TEMP_off['temp_off'],label="l_o = -1.5")
ax.plot(range(90,300),TEMP_off2['temp_off2'],label="l_o = -2")
legend = ax.legend()
plt.title("Variation of temperature wrt different offset intervals")
plt.savefig("new_temperature.png")
plt.show()

WRN_tau = numpy.genfromtxt("testWarning_tau.csv", names=["wrn_tau"])

plt.plot(range(90,210), WRN_tau['wrn_tau'])
plt.title("IDS warning lelvel for tau = 100")
plt.savefig("new_wrn_100.png")
plt.show()

WRN_tau2 = numpy.genfromtxt("testWarning_tau2.csv", names=["wrn_tau2"])

plt.plot(range(240,360), WRN_tau2['wrn_tau2'])
plt.title("IDS warning level for tau = 250")
plt.savefig("new_wrn_250.png")
plt.show()

WRN_tau3 = numpy.genfromtxt("testWarning_tau3.csv", names=["wrn_tau3"])

plt.plot(range(290,410), WRN_tau3['wrn_tau3'])
plt.title("IDS warning level for tau = 300")
plt.savefig("new_wrn_300.png")
plt.show()

STRESS_off = numpy.genfromtxt("testStress.csv", names=["stress_off"])
STRESS_off1 = numpy.genfromtxt("testStress_l1.csv", names=["stress_off1"])
STRESS_off2 = numpy.genfromtxt("testStress_l2.csv", names=["stress_off2"])

fix, ax = plt.subplots()
ax.plot(range(90,220),STRESS_off1['stress_off1'],label="l_o = -1")
ax.plot(range(90,220),STRESS_off['stress_off'],label="l_o = -1.5")
ax.plot(range(90,220),STRESS_off2['stress_off2'],label="l_o = -2")
legend = ax.legend()
plt.title("Variation of stress wrt different offset intervals")
plt.savefig("new_stress.png")
plt.show()

WRN_max = numpy.genfromtxt("testIntervalWarn.csv", names=["wrn_max"])
STRESS_max = numpy.genfromtxt("testIntervalSt.csv", names=["stress_max"])

plt.plot(range(0,50),WRN_max['wrn_max'])
plt.title("Evaluation of distances wrt warning over time")
plt.savefig("new_time_wrn.png")
plt.show()

plt.plot(range(0,50),STRESS_max['stress_max'])
plt.title("Evaluation of distances wrt stress over time")
plt.savefig("new_time_stress.png")
plt.show()


fix, ax = plt.subplots()
ax.plot(range(0,50),WRN_max['wrn_max'],label="warning")
ax.plot(range(0,50),STRESS_max['stress_max'],label="stress")
legend = ax.legend()
plt.title("Evaluation of distances over time")
plt.savefig("time.png")
plt.show()


Max_WRN = numpy.genfromtxt("testMaxWrn.csv", names=["max_wrn"])
plt.plot(range(0,50),Max_WRN['max_wrn'])
plt.title("Evaluation of distances wrt warning over time")
plt.savefig("new_time_max_wrn.png")
plt.show()




CI_left = numpy.genfromtxt("testBootstrapL_50.csv", names=["CIleft"])
CI_right = numpy.genfromtxt("testBootstrapR_50.csv", names=["CIright"])


CI_width = abs(CI_left['CIleft'] - CI_right['CIright'])
CI_mean = sum(CI_width)/len(CI_width)

print("Maximal difference for m=50 "+str(max(CI_width)))
print("Average difference for m=50 "+str(CI_mean))

fix, ax = plt.subplots()
ax.plot(range(0,50),CI_left['CIleft'],label="CI_l")
ax.plot(range(0,50),CI_right['CIright'],label="CI_r")
legend = ax.legend()
plt.title("Evaluation of confidence interval over time")
plt.savefig("new_CI.png")
plt.show()

CI_left_100 = numpy.genfromtxt("testBootstrapL_100.csv", names=["CIleft_100"])
CI_right_100 = numpy.genfromtxt("testBootstrapR_100.csv", names=["CIright_100"])

CI_width_100 = abs(CI_left_100['CIleft_100'] - CI_right_100['CIright_100'])
CI_mean_100 = sum(CI_width_100)/len(CI_width_100)

print("Maximal difference for m=100 "+str(max(CI_width_100)))
print("Average difference for m=100 "+str(CI_mean_100))


fix, ax = plt.subplots()
ax.plot(range(0,50),CI_left_100['CIleft_100'],label="CI_l")
ax.plot(range(0,50),CI_right_100['CIright_100'],label="CI_r")
legend = ax.legend()
plt.title("Evaluation of confidence interval over time")
plt.savefig("new_CI_100.png")
plt.show()

valuation_1 = numpy.genfromtxt("new_testThreeValue1.csv", names=["val1"])
valuation_2 = numpy.genfromtxt("new_testThreeValue2.csv", names=["val2"])
valuation_3 = numpy.genfromtxt("new_testThreeValue3.csv", names=["val3"])

fix, ax = plt.subplots()
ax.plot(range(0,50),valuation_3['val3'],label="eta_3=0.06")
ax.plot(range(0,50),valuation_2['val2'],label="eta_3=0.04")
ax.plot(range(0,50),valuation_1['val1'],label="eta_3=0.03")
legend = ax.legend()
plt.title("Evaluation of three-valued semantics")
plt.savefig("new_three_values.png")
plt.show()

slow_02 = numpy.genfromtxt("slow_02.csv", names=["s02"])
slow_02_30 = numpy.genfromtxt("slow_02_30.csv", names=["s0230"])
slow_02_50 = numpy.genfromtxt("slow_02_50.csv", names=["s0250"])

slow_03 = numpy.genfromtxt("slow_03.csv", names=["s03"])
slow_03_30 = numpy.genfromtxt("slow_03_30.csv", names=["s0330"])
slow_03_50 = numpy.genfromtxt("slow_03_50.csv", names=["s0350"])

slow_04 = numpy.genfromtxt("slow_04.csv", names=["s04"])
slow_04_30 = numpy.genfromtxt("slow_04_30.csv", names=["s0430"])
slow_04_50 = numpy.genfromtxt("slow_04_50.csv", names=["s0450"])


comb_02 = numpy.genfromtxt("comb_02.csv", names=["c02"])
comb_02_30 = numpy.genfromtxt("comb_02_30.csv", names=["c0230"])
comb_02_50 = numpy.genfromtxt("comb_02_50.csv", names=["c0250"])

comb_03 = numpy.genfromtxt("comb_03.csv", names=["c03"])
comb_03_30 = numpy.genfromtxt("comb_03_30.csv", names=["c0330"])
comb_03_50 = numpy.genfromtxt("comb_03_50.csv", names=["c0350"])

comb_04 = numpy.genfromtxt("comb_04.csv", names=["c04"])
comb_04_30 = numpy.genfromtxt("comb_04_30.csv", names=["c0430"])
comb_04_50 = numpy.genfromtxt("comb_04_50.csv", names=["c0450"])


fix, ax = plt.subplots()
ax.plot(range(0,10),slow_02['s02'],label="0.2")
ax.plot(range(0,10),slow_03['s03'],label="0.3")
ax.plot(range(0,10),slow_04['s04'],label="0.4")
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,10,20,30,40,50,60,70,80,90])
legend = ax.legend(loc='lower right')
plt.title("Evaluation of phi_slow, 10 steps")
plt.savefig("slow_three_values_10.png")
plt.show()

fix, ax = plt.subplots()
ax.plot(range(0,10),slow_02_30['s0230'],label="0.2")
ax.plot(range(0,10),slow_03_30['s0330'],label="0.3")
ax.plot(range(0,10),slow_04_30['s0430'],label="0.4")
legend = ax.legend(loc='lower right')
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,30,60,90,120,150,180,210,240,270])
plt.title("Evaluation of phi_slow, 30 steps")
plt.savefig("slow_three_values_30.png")
plt.show()

fix, ax = plt.subplots()
ax.plot(range(0,10),slow_02_50['s0250'],label="0.2")
ax.plot(range(0,10),slow_03_50['s0350'],label="0.3")
ax.plot(range(0,10),slow_04_50['s0450'],label="0.4")
legend = ax.legend()
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,50,100,150,200,250,300,350,400,450])
plt.title("Evaluation of phi_slow, 50 steps")
plt.savefig("slow_three_values_50.png")
plt.show()


fix, ax = plt.subplots()
ax.plot(range(0,10),comb_02['c02'],label="0.2")
ax.plot(range(0,10),comb_03['c03'],label="0.3")
ax.plot(range(0,10),comb_04['c04'],label="0.4")
legend = ax.legend(loc='lower right')
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,10,20,30,40,50,60,70,80,90])
plt.title("Evaluation of phi_comb, 10 steps")
plt.savefig("comb_three_values_10.png")
plt.show()

fix, ax = plt.subplots()
ax.plot(range(0,10),comb_02_30['c0230'],label="0.2")
ax.plot(range(0,10),comb_03_30['c0330'],label="0.3")
ax.plot(range(0,10),comb_04_30['c0430'],label="0.4")
legend = ax.legend(loc='lower right')
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,30,60,90,120,150,180,210,240,270])
plt.title("Evaluation of phi_comb, 30 steps")
plt.savefig("comb_three_values_30.png")
plt.show()

fix, ax = plt.subplots()
ax.plot(range(0,10),comb_02_50['c0250'],label="0.2")
ax.plot(range(0,10),comb_03_50['c0350'],label="0.3")
ax.plot(range(0,10),comb_04_50['c0450'],label="0.4")
legend = ax.legend()
ax.set_xticks([0,1,2,3,4,5,6,7,8,9])
ax.set_xticklabels([0,50,100,150,200,250,300,350,400,450])
plt.title("Evaluation of phi_comb, 50 steps")
plt.savefig("comb_three_values_50.png")
plt.show()

speedV1 = numpy.genfromtxt("real_speed_V1.csv", names=["speed1"])
speedV2 = numpy.genfromtxt("real_speed_V2.csv", names=["speed2"])

fix, ax = plt.subplots()
ax.plot(range(0,400),speedV1['speed1'],label="V1")
ax.plot(range(0,400),speedV2['speed2'],label="V2")
legend = ax.legend()
plt.title("Real speed of vehicles")
plt.savefig("speed.png")
plt.show()

