flight valid_charter {
    type CHARTER ;

    leg {
        departure LPPT 2026-05-10 09:00 ;
        arrival LEMD 2026-05-10 11:30 ;

        route {
            segment (38.77, -9.13) (40.47, -3.57)
                alt 28000 m width 500 m ;
                wind 180 12.0 m/s ;
        }

        fuel 3200.00 kg ;
        load passengers 80 pax_weight 6400.0 kg cargo_weight 500.0 kg ;
    }
}
