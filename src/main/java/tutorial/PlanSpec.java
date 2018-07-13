package tutorial;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.AtlassianModule;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.trigger.AnyTrigger;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;
import com.atlassian.bamboo.specs.util.BambooServer;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.util.MapBuilder;

//
//@BambooSpec
//public class PlanSpec {
//
//    /**
//     * Run 'main' to publish your plan.
//     */
//    public static void main(String[] args) throws Exception {
//        // by default credentials are read from the '.credentials' file
//        BambooServer bambooServer = new BambooServer("http://localhost:8085");
//
//        Plan plan = new PlanSpec().createPlan();
//        bambooServer.publish(plan);
//
//        PlanPermissions planPermission = new PlanSpec().createPlanPermission(plan.getIdentifier());
//        bambooServer.publish(planPermission);
//    }
//
//    PlanPermissions createPlanPermission(PlanIdentifier planIdentifier) {
//        Permissions permissions = new Permissions()
//                .userPermissions("admin", PermissionType.ADMIN)
//                .groupPermissions("bamboo-admin", PermissionType.ADMIN)
//                .loggedInUserPermissions(PermissionType.BUILD)
//                .anonymousUserPermissionView();
//
//        return new PlanPermissions(planIdentifier)
//                .permissions(permissions);
//    }
//
//    Project project() {
//        return new Project()
//                .name("My Project")
//                .key("PROJ");
//    }
//
//    Plan createPlan() {
//        return new Plan(project(), "The plan", "PLAN")
//                .description("Plan created from Bamboo Java Specs");
//    }
//}



@BambooSpec
public class PlanSpec {

    Project project() {
        return new Project()
                .name("My Project")
                .key("PROJ");
    }

    public Plan plan() {
        final Plan plan = new Plan(project(), "the second plan", "PLAN2");
        plan.description("Plan to experiment with bamboo");
        plan.pluginConfigurations(new ConcurrentBuilds()
                .useSystemWideDefault(false));
        plan.stages(new Stage("Default Stage")
                .jobs(new Job("Default Job",
                        new BambooKey("JOB1"))
                        .tasks(new VcsCheckoutTask()
                                        .description("Checkout Default Repository")
                                        .checkoutItems(new CheckoutItem().defaultRepository()),
                                new ScriptTask()
                                        .description("build")
                                        .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
                                        .inlineBody("dotnet build ${bamboo.build.working.directory}/vega.csproj"),
                                new ScriptTask()
                                        .description("test")
                                        .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
                                        .inlineBody("dotnet test ${bamboo.build.working.directory}/vega.csproj --no-build"),
                                new ScriptTask()
                                        .description("pack")
                                        .inlineBody("dotnet pack ${bamboo.build.working.directory}/vega.csproj --no-build"))));
        plan.linkedRepositories("net core / angular tuto");
        plan.triggers(new RepositoryPollingTrigger(),
                new AnyTrigger(new AtlassianModule("com.atlassian.bamboo.triggers.atlassian-bamboo-triggers:daily"))
                        .name("Single daily build")
                        .configuration(new MapBuilder()
                                .put("trigger.created.by.user", "vincenthetet")
                                .put("repository.change.daily.buildTime", "10:14")
                                .build()));
        plan.planBranchManagement(new PlanBranchManagement()
                .delete(new BranchCleanup())
                .notificationForCommitters());
        plan.forceStopHungBuilds();
        return plan;
    }

    public PlanPermissions planPermission() {
        final PlanPermissions planPermission = new PlanPermissions(new PlanIdentifier("TES", "TES"))
                .permissions(new Permissions()
                        .userPermissions("vincenthetet", PermissionType.EDIT, PermissionType.VIEW, PermissionType.ADMIN, PermissionType.CLONE, PermissionType.BUILD));
        return planPermission;
    }

    public static void main(String... argv) {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer("http://10.10.194.104:8085");
        final PlanSpec planSpec = new PlanSpec();

        final Plan plan = planSpec.plan();
        bambooServer.publish(plan);

        final PlanPermissions planPermission = planSpec.planPermission();
        bambooServer.publish(planPermission);
    }
}