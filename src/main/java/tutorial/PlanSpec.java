package tutorial;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;
import com.atlassian.bamboo.specs.util.BambooServer;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;

@BambooSpec
public class PlanSpec {

    Project project() {
        return new Project()
                .name("Vega Project Build Pipeline")
                .key("VPBP");
    }

    public Plan plan() {
        final Plan plan = new Plan(project(), "build plan", "PLAN");
        plan.description("A plan that build and test the vega project");
        plan.pluginConfigurations(new ConcurrentBuilds()
                .useSystemWideDefault(false));
        plan.stages(new Stage("build and test")
                .jobs(new Job("Build and test",
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
        plan.triggers(new RepositoryPollingTrigger());
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