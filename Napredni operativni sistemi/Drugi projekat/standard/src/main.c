/*
 * Copyright (c) 2018 STMicroelectronics
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <zephyr.h>
#include <device.h>
#include <drivers/sensor.h>
#include <stdio.h>
#include <sys/util.h>
#include <devicetree.h>
#include <drivers/gpio.h>

/* 1000 msec = 1 sec */
#define SLEEP_TIME_MS   1000

/* The devicetree node identifier for the "led0" alias. */
#define LED0_NODE DT_ALIAS(led0)

#if DT_NODE_HAS_STATUS(LED0_NODE, okay)
#define LED0	DT_GPIO_LABEL(LED0_NODE, gpios)
#define PIN	DT_GPIO_PIN(LED0_NODE, gpios)
#define FLAGS	DT_GPIO_FLAGS(LED0_NODE, gpios)
#else
/* A build error here means your board isn't set up to blink an LED. */
#error "Unsupported board: led0 devicetree alias is not defined"
#define LED0	""
#define PIN	0
#define FLAGS	0
#endif


#ifdef CONFIG_LSM6DSL_TRIGGER
static int lsm6dsl_trig_cnt;

static void lsm6dsl_trigger_handler(const struct device *dev,
				     struct sensor_trigger *trig)
{
	sensor_sample_fetch_chan(dev, SENSOR_CHAN_ALL);
	lsm6dsl_trig_cnt++;
}
#endif

#define LSM6DSL_DEVNAME		DT_LABEL(DT_INST(0, st_lsm6dsl))
#define HTS221_DEVNAME		DT_LABEL(DT_INST(0, st_hts221))
#define LPS22HB_DEVNAME		DT_LABEL(DT_INST(0, st_lps22hb_press))
#define LIS2DH_DEVNAME		DT_LABEL(DT_INST(0, st_lis2dh))
#define LIS2MDL_DEVNAME		DT_LABEL(DT_INST(0, st_lis2mdl))

void main(void)
{
	float HTS221_temp;
	struct sensor_value temp1, temp2, hum, press;
	struct sensor_value accel1[3], accel2[3];
	struct sensor_value gyro[3];
	struct sensor_value magn[3];
	const struct device *hts221 = device_get_binding(HTS221_DEVNAME);
	const struct device *lps22hb = device_get_binding(LPS22HB_DEVNAME);
	const struct device *lsm6dsl = device_get_binding(LSM6DSL_DEVNAME);
	const struct device *lsm303agr_a = device_get_binding(LIS2DH_DEVNAME);
	const struct device *lsm303agr_m = device_get_binding(LIS2MDL_DEVNAME);
#ifdef CONFIG_LSM6DSL_TRIGGER
	int cnt = 1;

#endif
	HTS221_temp = 0;
	long cnt_temp = 0;
	float HTS221_temp_avg = 0;
	const struct device *dev;
	bool led_is_on = true;
	int ret;
		
	dev = device_get_binding(LED0);
	if (dev == NULL) {
		return;
	}

	ret = gpio_pin_configure(dev, PIN, GPIO_OUTPUT_ACTIVE | FLAGS);
	if (ret < 0) {
		return;
	}

	if (hts221 == NULL) {
		printf("Could not get HTS221 device\n");
		return;
	}
	if (lps22hb == NULL) {
		printf("Could not get LPS22HB device\n");
		return;
	}
	if (lsm6dsl == NULL) {
		printf("Could not get LSM6DSL device\n");
		return;
	}
	if (lsm303agr_a == NULL) {
		printf("Could not get LSM303AGR Accel device\n");
		return;
	}
	if (lsm303agr_m == NULL) {
		printf("Could not get LSM303AGR Magn device\n");
		return;
	}

	/* set LSM6DSL accel/gyro sampling frequency to 104 Hz */
	struct sensor_value odr_attr;

	odr_attr.val1 = 208;
	odr_attr.val2 = 0;

	if (sensor_attr_set(lsm6dsl, SENSOR_CHAN_ACCEL_XYZ,
			    SENSOR_ATTR_SAMPLING_FREQUENCY, &odr_attr) < 0) {
		printk("Cannot set sampling frequency for accelerometer.\n");
		return;
	}

	if (sensor_attr_set(lsm6dsl, SENSOR_CHAN_GYRO_XYZ,
			    SENSOR_ATTR_SAMPLING_FREQUENCY, &odr_attr) < 0) {
		printk("Cannot set sampling frequency for gyro.\n");
		return;
	}

#ifdef CONFIG_LSM6DSL_TRIGGER
	struct sensor_trigger trig;

	trig.type = SENSOR_TRIG_DATA_READY;
	trig.chan = SENSOR_CHAN_ACCEL_XYZ;
	sensor_trigger_set(lsm6dsl, &trig, lsm6dsl_trigger_handler);
#endif

	while (1) {
		int ret;

		/* Get sensor samples */

		if (sensor_sample_fetch(hts221) < 0) {
			printf("HTS221 Sensor sample update error\n");
			return;
		}
		if (sensor_sample_fetch(lps22hb) < 0) {
			printf("LPS22HB Sensor sample update error\n");
			return;
		}
#ifndef CONFIG_LSM6DSL_TRIGGER
		if (sensor_sample_fetch(lsm6dsl) < 0) {
			printf("LSM6DSL Sensor sample update error\n");
			return;
		}
#endif
		ret = sensor_sample_fetch(lsm303agr_a);
		if (ret < 0 && ret != -EBADMSG) {
			printf("LSM303AGR Accel Sensor sample update error\n");
			return;
		}
		if (sensor_sample_fetch(lsm303agr_m) < 0) {
			printf("LSM303AGR Magn Sensor sample update error\n");
			return;
		}

		/* Get sensor data */

		sensor_channel_get(hts221, SENSOR_CHAN_AMBIENT_TEMP, &temp1);
		sensor_channel_get(hts221, SENSOR_CHAN_HUMIDITY, &hum);
		sensor_channel_get(lps22hb, SENSOR_CHAN_PRESS, &press);
		sensor_channel_get(lps22hb, SENSOR_CHAN_AMBIENT_TEMP, &temp2);
		sensor_channel_get(lsm6dsl, SENSOR_CHAN_ACCEL_XYZ, accel1);
		sensor_channel_get(lsm6dsl, SENSOR_CHAN_GYRO_XYZ, gyro);
		sensor_channel_get(lsm303agr_a, SENSOR_CHAN_ACCEL_XYZ, accel2);
		sensor_channel_get(lsm303agr_m, SENSOR_CHAN_MAGN_XYZ, magn);

		/* Display sensor data */

		/* Erase previous */
		printf("\0033\014");

		printf("X-NUCLEO-IKS01A2 sensor dashboard\n\n");
		
		cnt_temp++;
		HTS221_temp += sensor_value_to_double(&temp1);
		HTS221_temp_avg = HTS221_temp / cnt_temp;

		if(HTS221_temp_avg > 22){
			led_is_on = true;
			printf("LED is on in iteration %ld\n", cnt_temp);	
		}
		else{
			led_is_on = false;
			printf("LED is off in iteration %ld\n", cnt_temp);			
		}

		gpio_pin_set(dev, PIN, (int)led_is_on);

		/* temperature */
		printf("HTS221: Current temperature: %.1f C\n",
		      sensor_value_to_double(&temp1));
		
		printf("HTS221: Average temperature: %.1f C\n",
		      HTS221_temp_avg);


		/* humidity */
		printf("HTS221: Relative Humidity: %.1f%%\n",
		       sensor_value_to_double(&hum));

		/* pressure */
		printf("LPS22HB: Pressure:%.3f kpa\n",
		       sensor_value_to_double(&press));

		/* lps22hb temperature */
		printf("LPS22HB: Temperature: %.1f C\n",
		       sensor_value_to_double(&temp2));

		/* lsm6dsl accel */
		printf("LSM6DSL: Accel (m.s-2): x: %.1f, y: %.1f, z: %.1f\n",
		       sensor_value_to_double(&accel1[0]),
		       sensor_value_to_double(&accel1[1]),
		       sensor_value_to_double(&accel1[2]));

		/* lsm6dsl gyro */
		printf("LSM6DSL: Gyro (dps): x: %.3f, y: %.3f, z: %.3f\n",
		       sensor_value_to_double(&gyro[0]),
		       sensor_value_to_double(&gyro[1]),
		       sensor_value_to_double(&gyro[2]));

#if defined(CONFIG_LSM6DSL_TRIGGER)
		printf("%d:: lsm6dsl trig %d\n", cnt++, lsm6dsl_trig_cnt);
#endif

		/* lsm303agr accel */
		printf("LSM303AGR: Accel (m.s-2): x: %.1f, y: %.1f, z: %.1f\n",
		       sensor_value_to_double(&accel2[0]),
		       sensor_value_to_double(&accel2[1]),
		       sensor_value_to_double(&accel2[2]));

		/* lsm303agr magn */
		printf("LSM303AGR: Magn (gauss): x: %.3f, y: %.3f, z: %.3f\n",
		       sensor_value_to_double(&magn[0]),
		       sensor_value_to_double(&magn[1]),
		       sensor_value_to_double(&magn[2]));

		k_sleep(K_MSEC(2000));
	}
}
