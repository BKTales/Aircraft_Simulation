#include "simulator_includes.h"

void print_flight_plan(flight_plan_t plan){
    printf("\n======== Flight Plan ID: %d ========\n", plan.id);

    printf("ID:             %d\n", plan.id);
    printf("Route:          %s\n", plan.route);
    printf("Type:           %s\n", plan.type);
    printf("Departure time: %s\n", plan.departure_time);
    printf("Legs:           %d\n", plan.leg_count);

    for (int i = 0; i < plan.leg_count; i++)
    {
        leg_t *leg = &plan.legs[i];
        printf("\nLeg %d: %s -> %s\n", i, leg->departure_id, leg->arrival_id);
        printf("  Fuel: %.0f %s\n", leg->fuel.quantity, leg->fuel.unit);
        printf("  Segments:       %d\n", leg->segment_count);
        for (int j = 0; j < leg->segment_count; j++)
        {
            segment_t *seg = &leg->segments[j];
            printf("    [%s] (%.4f, %.4f) -> (%.4f, %.4f)\n",
                   seg->mode,
                   seg->start.latitude, seg->start.longitude,
                   seg->end.latitude, seg->end.longitude);
        }
    }
}

void print_flight_plans(program_t* prog) {
    for (int i = 0; i < prog->flight_plans_info->count; i++) {
        print_flight_plan(prog->flight_plans_info->plans[i]);
    }
    printf("=========================================\n");
}

