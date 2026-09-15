#ifndef CONSTANTS_H
#define CONSTANTS_H

/* Constantes de conversão */
#define FT_TO_M_L               0.3048
#define KT_TO_MS_L              0.514444444
#define FPM_TO_MS_L             (0.3048 / 60.0)
#define FL_TO_M_L               (100.0 * 0.3048)
#define KNOTS_TO_MS             0.514444444

/* Constantes Físicas e ISA */
#define G                       9.80665      /* Gravidade m/s² */
#define GRAVITY                 9.80665
#define EARTH_RADIUS_M          6371000.0
#define GAMMA_AIR               1.4
#define R_AIR                   287.05       /* J/(kg·K) */
#define GAS_CONSTANT            287.05
#define TROPOPAUSE_M            11000.0
#define T_0                     288.15       /* Temperatura ao nível do mar */
#define SEA_LEVEL_TEMP          288.15
#define P_0                     101325.0     /* Pressão ao nível do mar */
#define SEA_LEVEL_PRESSURE      101325.0
#define RHO_0                   1.225        /* Densidade ao nível do mar */
#define SEA_LEVEL_DENSITY       1.225
#define LAPSE_RATE              -0.0065      /* K/m */
#define TEMP_LAPSE_RATE         -0.0065
#define THRUST_DENSITY_EXPONENT 0.75

#ifndef M_PI
#define M_PI                    3.14159265358979323846
#endif
#define PI                      3.14159265358979323846

#endif /* CONSTANTS_H */
