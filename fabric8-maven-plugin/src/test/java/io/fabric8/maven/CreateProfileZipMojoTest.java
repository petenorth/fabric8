/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.maven;


import io.fabric8.maven.stubs.CreateProfileZipBundleProjectStub;
import io.fabric8.maven.stubs.CreateProfileZipJarProjectStub;
import io.fabric8.maven.stubs.CreateProfileZipProjectStub;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This test uses JUnit3 API because the Junit4 API for
 * maven plugin test framework depends on eclipse aether, but we use
 * sonatype aether.
 */
public class CreateProfileZipMojoTest extends AbstractMojoTestCase {

    private CreateProfileZipProjectStub projectStub;

    public static final String EXPECTED_EXCEPTION_MESSAGE =
            "The property artifactBundleClassifier " +
                    "was specified as '%s' without also specifying artifactBundleType";

    public static final String TEST_CLASSIFIER = "foo";

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttemptOverrideClassifier() throws Exception {

        // GIVEN

        pomWithJarPackaging();

        CreateProfileZipMojo mojo = createProfileZipMojoWithBasicConfig();

        // WHEN

        artifactBundleClassifierIsOverridden(mojo, TEST_CLASSIFIER);

        // THEN

        expectExceptionWhenExecuting(mojo,
                String.format(EXPECTED_EXCEPTION_MESSAGE,TEST_CLASSIFIER));

    }


    public void testOverrideWithZipType() throws Exception {

        // GIVEN

        pomWithJarPackaging();

        CreateProfileZipMojo mojo = createProfileZipMojoWithBasicConfig();

        // WHEN

        artifactBundleTypeIsOverridden(mojo, "zip");

        mojo.execute();

        // THEN

        bundleReferencesHaveZipExtension();

    }

    public void testDefaultJarType() throws Exception {

        // GIVEN

        pomWithJarPackaging();

        // WHEN

        createProfileZipMojoWithBasicConfig().execute();

        // THEN

        bundleReferencesHaveNoExtension();

    }

    public void testDefaultBundleType() throws Exception {

        // GIVEN

        pomWithBundlePackaging();

        // WHEN

        createProfileZipMojoWithBasicConfig().execute();

        // THEN

        bundleReferencesHaveNoExtension();

    }

    public void testExplicitJarType() throws Exception {

        // GIVEN

        pomWithJarPackaging();

        CreateProfileZipMojo mojo = createProfileZipMojoWithBasicConfig();

        // WHEN

        artifactBundleTypeIsOverridden(mojo, "jar");

        mojo.execute();

        // THEN

        bundleReferencesHaveJarExtension();

    }

    private void expectExceptionWhenExecuting(CreateProfileZipMojo mojo, String message) throws MojoExecutionException {
        try {
            mojo.execute();
            Assert.fail("Expected MojoFailureException not thrown");
        } catch (MojoFailureException e) {
            Assert.assertEquals(
                    message,
                    e.getMessage());
        }
    }

    private void bundleReferencesHaveJarExtension() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        assertPropertiesKeyExists(props, getArtifactBundleKey("jar"));
        assertPropertyValue(props, getArtifactBundleKey("jar"),
                getExpectedArtifactBundleValue("jar"));
    }

    private void bundleReferencesHaveNoExtension() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        assertPropertiesKeyExists(props, getArtifactBundleKey());
        assertPropertyValue(props, getArtifactBundleKey(),
                getExpectedArtifactBundleValue());
    }

    private void bundleReferencesHaveJarWithFooClassifier() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        assertPropertiesKeyExists(props, getArtifactBundleKey("jar/foo"));
        assertPropertyValue(props, getArtifactBundleKey("jar/foo"),
                getExpectedArtifactBundleValue("jar/foo"));
    }

    private void bundleReferencesHaveZipExtension() throws IOException {
        Properties props = loadProperties(getFabricAgentPropertiesFile(getGeneratedProfilesDir()));

        assertPropertiesKeyExists(props, getArtifactBundleKey("zip"));
        assertPropertyValue(props, getArtifactBundleKey("zip"),
                getExpectedArtifactBundleValue("zip"));
    }

    private CreateProfileZipMojo createProfileZipMojoWithBasicConfig() throws Exception {
        CreateProfileZipMojo profileZipMojo = (CreateProfileZipMojo) lookupMojo( "zip", getPom());

        assertNotNull(profileZipMojo);

        setVariableValueToObject(profileZipMojo,"buildDir", getGeneratedProfilesDir());

        setVariableValueToObject(profileZipMojo,"outputFile", getProfileZip());

        return profileZipMojo;
    }

    private void artifactBundleTypeIsOverridden(CreateProfileZipMojo mojo, String override) throws IllegalAccessException {
        setVariableValueToObject(mojo, "artifactBundleType", override);
    }

    private void artifactBundleClassifierIsOverridden(CreateProfileZipMojo mojo, String classifier) throws IllegalAccessException {
        setVariableValueToObject(mojo, "artifactBundleClassifier", classifier);
    }

    private void pomWithJarPackaging() {
        projectStub = new CreateProfileZipJarProjectStub();

        Assert.assertEquals("jar", getPackaging());

        Assert.assertEquals("jar", getArtifactType());
    }

    private void pomWithBundlePackaging() {
        projectStub = new CreateProfileZipBundleProjectStub();

        Assert.assertEquals("bundle", getPackaging());

        Assert.assertEquals("bundle", getArtifactType());
    }


    // helpers...

    private void assertPropertyValue(Properties props, String key, String expectedArtifactBundleValue) {
        Assert.assertEquals(expectedArtifactBundleValue, props.getProperty(key));
    }

    private void assertPropertiesKeyExists(Properties props, String key) {
        Assert.assertNotNull("No value for key " + key + ". Available keys: " + props.keySet(), props.getProperty(key));
    }

    private File getProfileZip() {
        return new File(getBasedir() + "/target/profile.zip");
    }

    private File getGeneratedProfilesDir() {
        return new File(getBasedir() + "/target/generated-profiles");
    }


    private String getExpectedArtifactBundleValue() {
        return "fab:mvn:" + getBundleGavSpec();
    }

    private String getExpectedArtifactBundleValue(String type) {
        return getExpectedArtifactBundleValue() + getTypeSpec(type);
    }

    private String getArtifactBundleKey() {
        return "bundle.fab:mvn:" + getBundleGavSpec();
    }

    private String getArtifactBundleKey(String type) {
        return getArtifactBundleKey() + getTypeSpec(type);
    }

    private String getTypeSpec(String type) {
        return "/" +type;
    }

    private String getBundleGavSpec() {
        return getGroupId() + "/" + getArtifactId() + "/" + getVersion();
    }

    private String getProfilePathComponent() {
        // profilePathComponent: looks like: io.fabric8.maven.test/zip/test.profile
        return getGroupId() + "/" +
                getArtifactId().replace('-', '/') + ".profile";
    }

    private String getVersion() {
        return projectStub.getVersion();
    }

    private String getGroupId() {
        return projectStub.getGroupId();
    }

    private String getArtifactType() {
        return projectStub.getArtifact().getType();
    }

    private String getPackaging() {
        return projectStub.getPackaging();
    }

    private File getFabricAgentPropertiesFile(File generatedProfiles) {
        return new File(generatedProfiles, getProfilePathComponent() +
                "/io.fabric8.agent.properties");
    }

    private String getPom() {
        return projectStub.getFile().toString();
    }

    private String getArtifactId() {
        return projectStub.getArtifactId();
    }

    private Properties loadProperties(File fabricAgentPropertiesFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(fabricAgentPropertiesFile));
        return props;
    }

}