# Scientific sample datasets

All seven datasets are **synthetic demonstrations**, generated for this analyzer. They are not field observations, experimental findings or instrument calibration data. Numeric fields use decimal points; units are in the headers. All files are UTF-8 comma-separated CSV.

Upload one file from this folder, select the suggested column, enter the unit label and click **Calculate statistics**. The chart uses data-row index even when a time column is present. JSON retains original values and record positions.

| Dataset | Rows | Suggested column | Unit | What to explore |
| --- | ---: | --- | --- | --- |
| [A week of weather](weather_week.csv) | 168 | `temperature_C` | °C | Seven daily temperature cycles, changing pressure and two rain events. |
| [Reaction decay](reaction_decay.csv) | 91 | `concentration_mg_L` | mg/L | A steep exponential decline that approaches a small residual level. |
| [Damped oscillation](damped_oscillation.csv) | 240 | `position_mm` | mm | Positive and negative oscillations whose amplitude decreases over time. |
| [Sensor drift and outliers](sensor_drift_outliers.csv) | 120 | `sensor_reading_kPa` | kPa | Gradual drift, an offset step and two deliberately extreme measurements. |
| [Greenhouse with missing measurements](greenhouse_missing_values.csv) | 96 | `temperature_C` | °C | Daily cycles, watering jumps and genuine empty numeric fields. |
| [Water quality across four sites](water_quality_sites.csv) | 80 | `turbidity_NTU` | NTU | Four labelled sites and a simulated downstream turbidity/nitrate pulse. |
| [Two particle populations](two_particle_populations.csv) | 160 | `diameter_um` | µm | Two clearly separated diameter groups, interleaved in sampling order. |

Try **sensor drift and outliers** to compare mean/median and the reference/reading/bias columns. Try **damped oscillation** for a graph with negative values. In **greenhouse data**, temperature has 90 valid values and 6 excluded blanks; humidity has 92 and 4; soil moisture has 91 and 5. The empty fields should leave gaps in the chart.

For **water quality**, select each measurement separately. pH has no unit; conductivity uses µS/cm, nitrate uses mg/L and turbidity uses NTU. Site labels are categorical. For **particle populations**, compare the combined summary with the two groups visible in the preview and the alternating profile. The analyzer does not perform grouping or statistical inference.

## Generation method

The simulated dates begin on 1 October 2026. A fixed seed of `20261009` drives a 32-bit linear congruential generator: `state = (1664525 * state + 1013904223) mod 2^32`; uniform jitter is `amplitude * (2 * state/2^32 - 1)`. Values are rounded before saving. The generation order follows the table above.

### A week of weather

168 hourly records. Temperature = 14 + 7*sin(2π*(hour-of-day-8)/24) + 0.3*day + uniform jitter ±0.6 °C. Humidity follows the opposite cycle. Rain occurs on simulated days 3 and 6.

### Reaction decay

91 one-minute records. Concentration = 80*exp(-0.055*time) + 1.8 + uniform jitter ±0.18 mg/L. Absorbance is a synthetic scaled signal, not a calibrated instrument result.

### Damped oscillation

240 records at 0.1-second intervals. Position = 12*exp(-0.12*time)*sin(2π*0.45*time) + uniform jitter ±0.07 mm. Model velocity is the noiseless derivative; envelope is the decay amplitude.

### Sensor drift and outliers

120 one-minute records. Reading adds 0.025 kPa per minute of drift and a +1.75 kPa offset from data row 71. Data rows 35 and 92 add spikes of +8.2 and -7 kPa. Bias is the rounded reading minus its reference.

### Greenhouse with missing measurements

96 half-hourly records. Temperature and humidity follow opposite daily cycles. Soil moisture declines by 0.08 percentage points per record, with watering jumps at data rows 25 and 69. There are 6 blank temperatures, 4 blank humidity values and 5 blank soil measurements. Missing values are empty fields, not zero or text.

### Water quality across four sites

20 sampling rounds × 4 sites = 80 records. Synthetic baselines differ by site. Downstream rounds 10–13 add a nitrate and turbidity pulse. Site/event labels deliberately include quoted commas, and one label contains UTF-8 text. pH is dimensionless. The app summarizes a whole column; it does not automatically group by site.

### Two particle populations

80 particles per group, 160 total. Group A diameters are 4.8 ±0.6 µm; group B diameters are 14.5 ±0.9 µm with uniform jitter. The combined mean lies between the clusters and is not a typical diameter of either group.

The original five-value `temperature.csv` remains the small hand-calculated reference fixture.
