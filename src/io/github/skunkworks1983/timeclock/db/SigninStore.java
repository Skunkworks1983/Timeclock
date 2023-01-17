package io.github.skunkworks1983.timeclock.db;

import java.util.List;

import static io.github.skunkworks1983.timeclock.db.generated.tables.Signins.SIGNINS;

public class SigninStore {
    public SigninStore() {}

    public List<Signin> getSignins() {
        List<Signin> signinList = DatabaseConnector
                .runQuery(query -> {
                    List<Signin> signins = query.selectFrom(SIGNINS)
                            .orderBy(SIGNINS.TIME.asc())
                            .fetch()
                            .into(Signin.class);
                    return signins;
                });

        return signinList;
    }
}
