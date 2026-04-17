package com.mondial.api.initializer;

import com.mondial.api.model.Nation;
import com.mondial.api.repository.NationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class NationDataInitializer implements CommandLineRunner {

    private final NationRepository nationRepository;

    public NationDataInitializer(NationRepository nationRepository) {
        this.nationRepository = nationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (nationRepository.count() > 0) {
            return;
        }

        List<Nation> nations = Arrays.asList(
                new Nation("Canada", "🇨🇦"),
                new Nation("Mexico", "🇲🇽"),
                new Nation("United States", "🇺🇸"),
                new Nation("Australia", "🇦🇺"),
                new Nation("Iran", "🇮🇷"),
                new Nation("Japan", "🇯🇵"),
                new Nation("Jordan", "🇯🇴"),
                new Nation("Qatar", "🇶🇦"),
                new Nation("Saudi Arabia", "🇸🇦"),
                new Nation("South Korea", "🇰🇷"),
                new Nation("Uzbekistan", "🇺🇿"),
                new Nation("Iraq", "🇮🇶"),
                new Nation("Algeria", "🇩🇿"),
                new Nation("Cape Verde", "🇨🇻"),
                new Nation("Egypt", "🇪🇬"),
                new Nation("Ivory Coast", "🇨🇮"),
                new Nation("Morocco", "🇲🇦"),
                new Nation("Nigeria", "🇳🇬"),
                new Nation("Senegal", "🇸🇳"),
                new Nation("South Africa", "🇿🇦"),
                new Nation("Tunisia", "🇹🇳"),
                new Nation("DR Congo", "🇨🇩"),
                new Nation("Costa Rica", "🇨🇷"),
                new Nation("Curaçao", "🇨🇼"),
                new Nation("Haiti", "🇭🇹"),
                new Nation("Honduras", "🇭🇳"),
                new Nation("Jamaica", "🇯🇲"),
                new Nation("Panama", "🇵🇦"),
                new Nation("Argentina", "🇦🇷"),
                new Nation("Brazil", "🇧🇷"),
                new Nation("Colombia", "🇨🇴"),
                new Nation("Ecuador", "🇪🇨"),
                new Nation("Paraguay", "🇵🇾"),
                new Nation("Uruguay", "🇺🇾"),
                new Nation("New Zealand", "🇳🇿"),
                new Nation("Austria", "🇦🇹"),
                new Nation("Belgium", "🇧🇪"),
                new Nation("Bosnia and Herzegovina", "🇧🇦"),
                new Nation("Croatia", "🇭🇷"),
                new Nation("Czechia", "🇨🇿"),
                new Nation("England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"),
                new Nation("France", "🇫🇷"),
                new Nation("Germany", "🇩🇪"),
                new Nation("Netherlands", "🇳🇱"),
                new Nation("Norway", "🇳🇴"),
                new Nation("Portugal", "🇵🇹"),
                new Nation("Scotland", "🏴󠁧󠁢󠁳󠁣󠁴󠁿"),
                new Nation("Spain", "🇪🇸"),
                new Nation("Sweden", "🇸🇪"),
                new Nation("Switzerland", "🇨🇭"),
                new Nation("Türkiye", "🇹🇷")
        );

        nationRepository.saveAll(nations);
    }
}

