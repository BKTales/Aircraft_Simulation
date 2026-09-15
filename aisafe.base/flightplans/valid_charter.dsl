flight valid_charter {
    type CHARTER ;
    load 80 6900 ;

    leg {
        departure LPPT 2026-05-10 09:00 ;
        arrival LEMD 2026-05-10 11:30 ;

        route {
            segment (38.77, -9.13) (40.47, -3.57)
                alt 28000 m width 500 m ;
                wind 180 12.0 m/s ;
        }

        fuel 3200.00 kg ;
        
    }
}
